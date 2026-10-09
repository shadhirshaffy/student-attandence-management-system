# Student Attendance Management System

A role-based web application for managing modules, enrollments, attendance sessions, QR attendance, manual corrections, absence justifications, student attendance summaries, and lecturer reports.

## Screenshots

Add screenshots here after starting both applications:

- Admin dashboard and user management
- Lecturer session QR display and manual attendance screen
- Student QR scan and attendance history screen

## Features

- JWT login for administrators, lecturers, and students
- Admin user creation, editing, activation, and deactivation
- Module creation, lecturer assignment, activation, and student enrollment
- Lecturer-owned sessions with scheduled, active, locked, and completed states
- Cryptographically random, expiring QR attendance links
- QR attendance for enrolled students and lecturer manual attendance management
- Manual attendance audit fields for who changed a record, when, and why
- Student absence justification submission with lecturer review endpoints
- Duplicate attendance protection at service and database levels
- Attendance percentages and low-attendance warnings (default threshold: 80%)
- Role-scoped attendance reports with CSV and PDF downloads

## Architecture

```text
Vue 3 + Pinia + Vue Router
          |
       Axios / REST
          |
Spring Boot + Security + JPA
          |
      PostgreSQL
```

The frontend authenticates requests with a bearer token. The backend derives the current user from that token, enforces role and ownership rules, and stores attendance in PostgreSQL.

## Technology stack

- Frontend: Vue 3, Vite, Pinia, Vue Router, Axios, Tailwind CSS, `html5-qrcode`, `qrcode`
- Backend: Java 25, Spring Boot 4, Spring Security, Spring Data JPA, Flyway, Maven, Springdoc OpenAPI
- Database: PostgreSQL 18 (PostgreSQL 16+ should also work)

## Prerequisites

- JDK 25
- Maven Wrapper (included) or Maven 3.9+
- Node.js 24+
- PostgreSQL 16+

## PostgreSQL setup

In `psql` as a PostgreSQL administrator, create a local database and application role:

```sql
CREATE USER attendance_app WITH PASSWORD 'choose-a-strong-local-password';
CREATE DATABASE attendance_db OWNER attendance_app;
```

Use a real private password locally. Do not commit it.

Database schema changes are managed with Flyway migrations in `backend/src/main/resources/db/migration`. Keep production on `spring.jpa.hibernate.ddl-auto=validate`; use new `V*__description.sql` files for future schema changes.

## Environment configuration

Copy the examples, then supply your own values. `backend/.env.example` is a reference list: set its values in PowerShell, an IDE run configuration, or your operating-system environment because Spring Boot does not load `.env` files itself.

```powershell
Copy-Item backend\src\main\resources\application-dev.example.properties backend\src\main\resources\application-dev.properties
Copy-Item frontend\.env.example frontend\.env
```

Generate a JWT secret with at least 32 random bytes:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Set the backend variables in the same terminal before starting it:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/attendance_db'
$env:DATABASE_USERNAME = 'attendance_app'
$env:DATABASE_PASSWORD = 'your-local-database-password'
$env:JWT_SECRET = 'your-generated-secret-at-least-32-bytes'
$env:FRONTEND_BASE_URL = 'http://localhost:5173'
$env:APP_SEED_ENABLED = 'true'
```

`APP_SEED_ENABLED` is intentionally off by default. Set it to `true` only for local development.

## Run the project

Open two terminals from this repository.

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The API runs at `http://localhost:8080/api`.

## Swagger

With the backend running, open `http://localhost:8080/swagger-ui.html`.

## Development seed accounts

These accounts are created only when the `dev` profile is active and `APP_SEED_ENABLED=true`:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@example.com` | `Admin@12345` |
| Lecturer | `lecturer@example.com` | `Lecturer@12345` |
| Student | `student@example.com` | `Student@12345` |

Change or remove seed accounts before any shared deployment.

## Testing

Backend tests use an isolated H2 database by default:

```powershell
cd backend
.\mvnw.cmd clean verify
```

If you use Git Bash, the equivalent requested wrapper command is:

```bash
cd backend
./mvnw clean verify
```

Build the frontend production bundle:

```powershell
cd frontend
npm test
npm run test:e2e
npm run build
```

The frontend unit suite covers stores, route guards, and reusable display components. The Playwright E2E suite covers admin sign-in, student route protection, and lecturer session creation.

The backend test suite covers login, token validation, authorization, user and enrollment duplicates, lecturer session ownership, QR lifecycle and eligibility, manual attendance audit behavior, statistics, report exports, concurrent QR requests, schema constraints, and CORS.

## Docker deployment

Copy the production environment example and set private values before starting the stack:

```powershell
Copy-Item .env.production.example .env.production
```

At minimum, change `POSTGRES_PASSWORD`, `DATABASE_PASSWORD`, and `JWT_SECRET`. Then start PostgreSQL, the backend, and the frontend:

```powershell
docker compose --env-file .env.production up --build
```

The frontend is published at `http://localhost:5173`, the backend API at `http://localhost:8080/api`, and Swagger at `http://localhost:8080/swagger-ui.html`.

For shared or production deployments, keep `APP_SEED_ENABLED=false`, use a private JWT secret, and replace the local `FRONTEND_BASE_URL` / `VITE_API_BASE_URL` values with the deployed URLs.

## Project structure

```text
student-attendance-management-system/
├── backend/
│   ├── .env.example
│   ├── Dockerfile
│   ├── src/main/resources/db/migration/
│   │   └── V1__initial_schema.sql
│   ├── src/main/java/com/studentattendance/
│   │   ├── controller/     REST endpoints
│   │   ├── service/        business rules and exports
│   │   ├── entity/         JPA database model
│   │   ├── repository/     data access
│   │   ├── security/       JWT and Spring Security
│   │   └── dto/            request and response models
│   └── src/test/           integration, schema, and export tests
├── frontend/
│   ├── .env.example
│   ├── Dockerfile
│   ├── playwright.config.js
│   └── src/
│       ├── views/          role-specific pages
│       ├── stores/         Pinia state and API actions
│       ├── components/     shared interface components
│       ├── layouts/        application shells
│       └── router/         navigation guards
├── docker-compose.yml
└── README.md
```

## Major API endpoints

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /api/auth/login`, `GET /api/auth/me` |
| Admin users | `GET/POST /api/admin/users`, `GET/PUT /api/admin/users/{id}`, `PATCH /api/admin/users/{id}/status` |
| Modules and enrollment | `GET/POST /api/modules`, `GET/PUT /api/modules/{id}`, `POST/DELETE /api/enrollments`, `GET /api/modules/{id}/students` |
| Sessions | `POST /api/sessions`, `GET /api/lecturers/me/sessions`, `PATCH /api/sessions/{id}/activate`, `/lock`, `/complete` |
| Attendance | `GET/PUT /api/sessions/{id}/attendance/manual`, `GET /api/sessions/{id}/qr`, `POST /api/attendance/qr` |
| Absence justifications | `GET/POST /api/students/me/absence-justifications`, `GET /api/lecturers/me/absence-justifications`, `PATCH /api/lecturers/me/absence-justifications/{id}` |
| Statistics | `GET /api/students/me/attendance-summary`, `GET /api/lecturers/me/attendance-summary`, `GET /api/admin/attendance-summary` |
| Reports | `GET /api/reports/attendance`, `/csv`, `/pdf` |

## Quality checklist

- Backend tests: `backend\mvnw.cmd clean verify`
- Frontend unit tests: `npm test`
- Frontend E2E tests: `npm run test:e2e`
- Frontend production build: `npm run build`
- Docker Compose config check: `docker compose config`
- Database migrations: `backend/src/main/resources/db/migration`

## Known limitations

- Playwright uses local Chrome on this development machine when Playwright browser downloads are unavailable.
- Absence justifications support submission and lecturer review APIs; a dedicated lecturer review screen can be added next.
- Notifications are not implemented yet.

## Future improvements

- Add a lecturer absence-justification review page.
- Add email or in-app notifications for low attendance and reviewed justifications.
- Expand E2E coverage to include admin enrollment, student QR submission, manual attendance correction, and report downloads.
- Add screenshots to the README after running the app locally.

## Troubleshooting

- **`mvn` or `mvnd` is not recognized:** reopen the terminal after updating Windows `Path`, or use `backend\mvnw.cmd`.
- **Backend cannot connect to PostgreSQL:** confirm the service is running, the database and role exist, and `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` match them.
- **Backend rejects startup:** set a `JWT_SECRET` that is at least 32 bytes long.
- **Browser shows a network or CORS error:** start the backend and keep `FRONTEND_BASE_URL` equal to the frontend address (`http://localhost:5173` by default).
- **QR camera is unavailable:** grant camera access to the local site or paste the attendance token into the scan form.
- **A session is excluded from percentages/reports:** sessions count only after the lecturer completes them. Locking blocks further marks; completing finalizes it for reporting.
