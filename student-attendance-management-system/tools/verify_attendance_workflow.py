"""Verify real PostgreSQL persistence using a disposable module and isolated backend."""
from pathlib import Path
import datetime, json, os, re, secrets, subprocess, time, urllib.request, urllib.error
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'evidence'
OUT.mkdir(parents=True, exist_ok=True)
config = (ROOT / 'backend/src/main/resources/application-dev.properties').read_text()
def setting(key):
    value = re.search(r'^'+re.escape(key)+r'=(.*)$', config, re.M).group(1).strip()
    match = re.fullmatch(r'\$\{([^}:]+)(?::(.*))?\}', value)
    if match:
        value = os.environ.get(match[1], match[2])
    if value is None: raise RuntimeError('Missing setting: '+key)
    return value
url = setting('spring.datasource.url')
user = setting('spring.datasource.username')
password = setting('spring.datasource.password')
match = re.fullmatch(r'jdbc:postgresql://([^:/]+)(?::(\d+))?/([^?]+)(?:\?.*)?', url)
if not match: raise RuntimeError('Unsupported local PostgreSQL URL')
host, port, database = match[1], match[2] or '5432', match[3]
if host not in ('localhost', '127.0.0.1'): raise RuntimeError('This check is limited to local PostgreSQL')
psql = Path(r'C:\Program Files\PostgreSQL\18\bin\psql.exe')
java = Path(os.environ['JAVA_HOME']) / 'bin/java.exe'
env = os.environ.copy()
env.update(DATABASE_URL=url, DATABASE_USERNAME=user, DATABASE_PASSWORD=password,
           JWT_SECRET=secrets.token_urlsafe(48), SERVER_PORT='18087', APP_SEED_ENABLED='false',
           SPRING_PROFILES_ACTIVE='dev', FLYWAY_ENABLED='false', JPA_DDL_AUTO='validate', PGPASSWORD=password)
base = 'http://127.0.0.1:18087/api'
process = None
log = None
module_id = None
token = None
events = []
code = 'WORKFLOW-' + datetime.datetime.now().strftime('%Y%m%d%H%M%S')
flags = subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0

def record(step, **data):
    event = {'step': step, **data}
    events.append(event)
    print(json.dumps(event), flush=True)

def sql(query):
    result = subprocess.run([str(psql), '-X', '-w', '-h', host, '-p', port, '-U', user,
                             '-d', database, '-At', '-v', 'ON_ERROR_STOP=1', '-c', query],
                            env=env, capture_output=True, text=True, creationflags=flags, check=True)
    return result.stdout.strip()

def api(method, path, payload=None, auth=None):
    headers = {'Content-Type': 'application/json'}
    if auth: headers['Authorization'] = 'Bearer '+auth
    request = urllib.request.Request(base+path, data=json.dumps(payload).encode() if payload is not None else None,
                                     headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            body = response.read()
            return response.status, json.loads(body) if body else None
    except urllib.error.HTTPError as error:
        return error.code, json.loads(error.read())

def start():
    global process, log
    log = (OUT / 'workflow-backend.log').open('a')
    process = subprocess.Popen([str(java), '-jar', str(ROOT/'backend/target/backend-0.0.1-SNAPSHOT.jar'),
        '--spring.config.additional-location=file:./src/main/resources/'], cwd=ROOT/'backend', env=env,
        stdout=log, stderr=subprocess.STDOUT, creationflags=flags)
    for _ in range(90):
        if process.poll() is not None: raise RuntimeError('Backend stopped; inspect workflow-backend.log')
        try:
            status, _ = api('GET', '/health')
            if status == 200:
                record('backend_started', pid=process.pid)
                return
        except (OSError, TimeoutError): pass
        time.sleep(1)
    raise RuntimeError('Backend startup timed out')

def stop():
    global process, log
    if process and process.poll() is None:
        pid = process.pid
        process.terminate()
        process.wait(timeout=30)
        record('backend_stopped', pid=pid)
    if log: log.close()
    process = None

def login():
    status, body = api('POST', '/auth/login', {'email':'admin@example.com', 'password':'Admin@12345'})
    if status != 200: raise RuntimeError('Development admin login unavailable; a valid local admin login is required')
    return body['token']

def row():
    return sql(f"SELECT row_to_json(m) FROM (SELECT id, module_code, module_name FROM modules WHERE id={int(module_id)}) m")

session_id = None
enrollment_id = None

def sign_in(email, password):
    status, body = api('POST', '/auth/login', {'email':email, 'password':password})
    assert status == 200, 'Development account login failed: '+email
    return body['token']

def expect(method, path, expected, payload=None, auth=None):
    status, body = api(method, path, payload, auth)
    assert status == expected, (method, path, status, body)
    return body

try:
    record('database', engine=sql('SELECT version()'), database=database)
    start()
    token = login()
    lecturer_token = sign_in('lecturer@example.com', 'Lecturer@12345')
    student_token = sign_in('student@example.com', 'Student@12345')
    expect('GET', '/admin/users', 403, auth=student_token)
    users = expect('GET', '/admin/users', 200, auth=token)
    lecturer = next(u for u in users if u['email'] == 'lecturer@example.com')
    student = next(u for u in users if u['email'] == 'student@example.com')
    payload = {'moduleCode':code, 'moduleName':'Temporary workflow verification', 'lecturerId':lecturer['profileId'], 'active':True}
    module = expect('POST','/modules',201,payload,token)
    module_id = module['id']
    payload['moduleName'] = 'Updated workflow verification'
    expect('PUT',f'/modules/{module_id}',200,payload,token)
    record('module_create_update', passed=True, module_id=module_id)
    available = expect('GET','/students/me/available-modules',200,auth=student_token)
    assert any(m['id']==module_id for m in available)
    enrollment = expect('POST',f'/students/me/modules/{module_id}',201,auth=student_token)
    enrollment_id = enrollment['id']
    expect('POST',f'/students/me/modules/{module_id}',409,auth=student_token)
    record('student_self_enrollment_and_duplicate_protection',passed=True)
    session = expect('POST','/sessions',201,{'moduleId':module_id,'sessionDate':(datetime.date.today()+datetime.timedelta(days=1)).isoformat(),'startTime':'09:00','endTime':'10:00','room':'Temporary test'},lecturer_token)
    session_id = session['id']
    active = expect('PATCH',f'/sessions/{session_id}/activate',200,auth=lecturer_token)
    qr_token = active['attendanceToken']
    expect('POST','/attendance/qr',200,{'token':qr_token},student_token)
    expect('POST','/attendance/qr',409,{'token':qr_token},student_token)
    count = sql(f'SELECT count(*) FROM attendance_records WHERE session_id={int(session_id)} AND student_id={int(student["profileId"])}')
    assert count=='1'
    record('qr_attendance_and_duplicate_protection',passed=True,postgres_rows=int(count))
    # Unchanged QR records should stay QR when a manual correction is not needed.
    manual = expect('GET',f'/sessions/{session_id}/attendance/manual',200,auth=lecturer_token)
    assert any(s['studentId']==student['profileId'] and s['status']=='PRESENT' for s in manual['students'])
    expect('PUT',f'/sessions/{session_id}/attendance/manual',200,{'records':[{'studentId':student['profileId'],'status':'ABSENT','reason':'Temporary workflow correction test'}]},lecturer_token)
    audit = sql(f"SELECT status || '|' || marking_method || '|' || manual_change_reason FROM attendance_records WHERE session_id={int(session_id)}")
    assert audit=='ABSENT|MANUAL|Temporary workflow correction test'
    expect('PUT',f'/sessions/{session_id}/attendance/manual',200,{'records':[{'studentId':student['profileId'],'status':'PRESENT','reason':'Restore temporary test mark'}]},lecturer_token)
    expect('PATCH',f'/sessions/{session_id}/lock',200,auth=lecturer_token)
    expect('POST','/attendance/qr',400,{'token':qr_token},student_token)
    expect('PUT',f'/sessions/{session_id}/attendance/manual',400,{'records':[{'studentId':student['profileId'],'status':'ABSENT','reason':'Must be rejected'}]},lecturer_token)
    expect('PATCH',f'/sessions/{session_id}/complete',200,auth=lecturer_token)
    record('manual_audit_lock_complete',passed=True)
    report = expect('GET',f'/reports/attendance?moduleId={module_id}',200,auth=token)
    assert len(report['rows'])==1 and report['rows'][0]['attendancePercentage']==100
    assert report['rows'][0]['sessionsHeld']==1 and report['rows'][0]['sessionsPresent']==1
    for format in ['csv','pdf']:
        req=urllib.request.Request(base+f'/reports/attendance/{format}?moduleId={module_id}',headers={'Authorization':'Bearer '+token})
        with urllib.request.urlopen(req,timeout=15) as response:
            data=response.read()
            assert data and (data.startswith(b'%PDF') if format=='pdf' else code.encode() in data)
            record('export_'+format,passed=True,bytes=len(data))
    record('report_calculation',passed=True,held=1,present=1,percentage=100)
    expect('DELETE',f'/modules/{module_id}',409,auth=token)
    stop()
    start()
    token=login()
    student_token=sign_in('student@example.com','Student@12345')
    assert expect('GET',f'/modules/{module_id}',200,auth=token)['moduleName']==payload['moduleName']
    assert any(m['id']==module_id for m in expect('GET','/students/me/modules',200,auth=student_token))
    assert expect('GET',f'/reports/attendance?moduleId={module_id}',200,auth=token)['rows'][0]['attendancePercentage']==100
    record('workflow_survived_backend_restart',passed=True)
    record('result',passed=True)
finally:
    stop()
    if module_id:
        # Delete only rows belonging to the disposable module created by this run.
        assert sql(f"SELECT module_code FROM modules WHERE id={int(module_id)}")==code
        sql(f"BEGIN; DELETE FROM absence_justifications WHERE session_id IN (SELECT id FROM attendance_sessions WHERE module_id={int(module_id)}); DELETE FROM attendance_records WHERE session_id IN (SELECT id FROM attendance_sessions WHERE module_id={int(module_id)}); DELETE FROM attendance_sessions WHERE module_id={int(module_id)}; DELETE FROM enrollments WHERE module_id={int(module_id)}; DELETE FROM modules WHERE id={int(module_id)}; COMMIT;")
        record('temporary_data_removed',module_id=module_id)
    (OUT/'workflow-results.json').write_text(json.dumps(events,indent=2)+'\n')
