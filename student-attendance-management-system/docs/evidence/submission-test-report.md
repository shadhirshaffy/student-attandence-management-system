# Submission verification - 30 October 2026

## Automated checks

| Check | Result |
| --- | --- |
| Backend: clean Maven verify, isolated H2 database | 31 passed; 0 failures; 0 errors |
| Frontend unit tests, single forks worker | 12 passed |
| Vite production build | Passed |
| Playwright browser suite | 12 passed |
| Git diff whitespace check | Passed |

Browser checks cover admin login, role redirects, session creation, safe module CRUD, module search/filter/sort, lecturer/student dashboards and mobile navigation, session filters, mobile QR display, expired-token feedback, token submission, report date validation, CSV downloads, selective manual corrections, and student self-enrollment after reload. Browser APIs are mocked; the separate PostgreSQL check below exercises the real backend.

## Real PostgreSQL workflow

Passed against PostgreSQL 18.6 in local `attendence_db`, using the newly built JAR on isolated port 18087:

- Module creation and update.
- Student self-enrollment and duplicate rejection.
- QR attendance stored as one PostgreSQL record; repeated submission rejected.
- Manual correction with status, marking method, and reason checked through SQL.
- Locked-session QR and manual submissions rejected; session completion succeeded.
- Report arithmetic: 1 completed session, 1 present, 100% attendance.
- CSV contents and PDF signature verified through real HTTP downloads.
- Deletion blocked for the used module.
- Module edits, enrollment, and report results retained after a full backend process restart.
- Disposable module, enrollment, session, and attendance records removed after verification. Existing records were not changed. Both isolated backend processes were stopped.

Raw evidence: [workflow-results.json](workflow-results.json). Reproduce using [verify_attendance_workflow.py](../../tools/verify_attendance_workflow.py) after building the backend and configuring the local database and development accounts. The check requires local PostgreSQL 18 tools and JAVA_HOME. It creates temporary data and removes only records belonging to its own disposable module. Backend logs are local Git-ignored files.

## Items still requiring attention

1. **Current running backend rejects the mobile origin.** An OPTIONS request from `http://localhost:5173` returned 200; `http://10.116.62.67:5173` returned 403. Restart the backend with FRONTEND_BASE_URL set to the LAN frontend URL and use that same URL on laptop and phone. This runtime configuration is separate from the isolated backend that passed the workflow check.
2. **Physical phone camera scanning remains untested.** Browser camera access needs a trusted HTTPS origin on the phone; the current LAN HTTP address supports manual token submission.
3. Docker deployment was not tested because Docker is not available in this shell. A fresh-machine setup was not performed.

Conclusion: automated and real PostgreSQL application checks passed. Mobile connectivity and physical-camera scanning must be verified before claiming every deployment path works.
