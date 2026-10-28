# PostgreSQL persistence verification

Date: 29 October 2026 (Asia/Colombo)

Result: PASS against real PostgreSQL 18.6, local database `attendence_db`.

This check used HTTP requests to the packaged Spring Boot backend and independent `psql` queries. It did not use mocked API responses or H2.

| Operation | Direct PostgreSQL verification | Verification after stopping and restarting Java |
| --- | --- | --- |
| Create | Module ID 1, code `PERSIST-20261029000818`, stored with name `Persistence proof - created` | GET returned 200 with the original name |
| Update | Same row stored with name `Persistence proof - updated` | GET returned 200 with the updated name |
| Delete | SQL returned zero rows for the module ID | GET returned 404; SQL still returned zero rows |

Four distinct backend processes were used: 40684, 31432, 3568, 15136. All were stopped after the check. The isolated backend ran on port 18087. The temporary module was removed.

## Local schema preparation

The existing database was older than the current code. Before testing, a PostgreSQL custom-format backup was saved to `backend/target/persistence-before-upgrade.dump` (Git ignored). The additive script `tools/upgrade_legacy_local_schema.sql` created the missing `absence_justifications` table and added the `marked_by_user_id` and `manual_change_reason` audit columns to `attendance_records`. Existing data was not reset. Hibernate schema validation passed afterward; Flyway and schema auto-updates were disabled throughout the persistence run.

## Evidence and reproduction

- Machine-readable results: [persistence-results.json](persistence-results.json)
- Backend startup logs are generated locally as `persistence-backend.log` and excluded from Git.
- Check script: [verify_persistence.py](../../tools/verify_persistence.py)

From the project directory, run `python tools/verify_persistence.py` with local database credentials configured in the ignored development properties file or environment. The current script requires PostgreSQL 18 tools, JAVA_HOME, the built backend JAR, and the existing development admin account and an active lecturer. It creates and removes one disposable module. No credentials are included in the evidence.

Scope: proves persistence through the real API and PostgreSQL across backend process restarts. Browser refresh behavior and PostgreSQL server restarts were not part of this check.
