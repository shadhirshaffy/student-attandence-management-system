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
code = 'PERSIST-' + datetime.datetime.now().strftime('%Y%m%d%H%M%S')
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
    log = (OUT / 'persistence-backend.log').open('a')
    process = subprocess.Popen([str(java), '-jar', str(ROOT/'backend/target/backend-0.0.1-SNAPSHOT.jar'),
        '--spring.config.additional-location=file:./src/main/resources/'], cwd=ROOT/'backend', env=env,
        stdout=log, stderr=subprocess.STDOUT, creationflags=flags)
    for _ in range(90):
        if process.poll() is not None: raise RuntimeError('Backend stopped; inspect persistence-backend.log')
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

try:
    version = sql('SELECT version()')
    record('database_connected', database=database, engine=version)
    # Do not reset the schema, run seeders, or change existing records.
    start()
    token = login()
    status, users = api('GET', '/admin/users', auth=token)
    assert status == 200
    lecturer = next(u for u in users if u['role'] == 'LECTURER' and u['active'])
    payload = {'moduleCode':code, 'moduleName':'Persistence proof - created',
               'description':'Temporary record for persistence verification; safe to remove.',
               'lecturerId':lecturer['profileId'], 'active':True}
    status, body = api('POST', '/modules', payload, token)
    assert status == 201, (status, body)
    module_id = body['id']
    assert json.loads(row())['module_name'] == payload['moduleName']
    record('create_verified_in_postgresql', module=json.loads(row()))
    stop()
    start()
    token = login()
    status, body = api('GET', f'/modules/{module_id}', auth=token)
    assert status == 200 and body['moduleName'] == payload['moduleName']
    record('create_survived_restart', module_id=module_id, name=body['moduleName'])
    payload['moduleName'] = 'Persistence proof - updated'
    status, body = api('PUT', f'/modules/{module_id}', payload, token)
    assert status == 200
    assert json.loads(row())['module_name'] == payload['moduleName']
    record('update_verified_in_postgresql', module=json.loads(row()))
    stop()
    start()
    token = login()
    status, body = api('GET', f'/modules/{module_id}', auth=token)
    assert status == 200 and body['moduleName'] == payload['moduleName']
    record('update_survived_restart', module_id=module_id, name=body['moduleName'])
    status, body = api('DELETE', f'/modules/{module_id}', auth=token)
    assert status == 204, (status, body)
    assert row() == ''
    record('delete_verified_in_postgresql', module_id=module_id, rows=0)
    stop()
    start()
    token = login()
    status, body = api('GET', f'/modules/{module_id}', auth=token)
    assert status == 404 and row() == ''
    record('delete_survived_restart', module_id=module_id, api_status=status, rows=0)
    record('result', passed=True)
finally:
    if module_id and process and process.poll() is None and token:
        if row():
            status, _ = api('DELETE', f'/modules/{module_id}', auth=token)
            record('cleanup', module_id=module_id, api_status=status)
    stop()
    (OUT/'persistence-results.json').write_text(json.dumps(events, indent=2)+'\n')
