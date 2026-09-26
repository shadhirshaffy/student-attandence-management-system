# Student Attendance Management System

Full-stack web application for managing student attendance. Phase 1 creates the project foundation only.

## Stack

- Frontend: Vue 3, Composition API, Vite, Tailwind CSS, Vue Router, Pinia, Axios
- Backend: Java, Spring Boot, Maven, Spring Web, Spring Security, Spring Data JPA, Validation, Springdoc OpenAPI
- Database: PostgreSQL

## Prerequisites

- Java 25
- Maven, or use the included Maven Wrapper in `backend`
- Node.js 24 or newer
- PostgreSQL 16 or newer for database-backed development phases

## Backend Setup

From the project root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The default `local` profile runs the Phase 1 health endpoint without requiring PostgreSQL.

Health check:

```text
http://localhost:8080/api/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

For PostgreSQL-backed development, copy `backend/src/main/resources/application-dev.example.properties` to `application-dev.properties`, provide environment variables, and run with:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Required development database variables:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/attendence_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_database_password
```

## Frontend Setup

From the project root:

```powershell
cd frontend
npm install
Copy-Item .env.example .env
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

Frontend API configuration:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Phase 1 Scope

Implemented:

- Backend Spring Boot project foundation
- Backend package structure
- Swagger/OpenAPI configuration
- Public `/api/health` endpoint
- Central exception handler foundation
- Frontend Vue/Vite/Tailwind foundation
- Vue Router, Pinia, and Axios setup
- Minimal home page
- Example environment/configuration files

Not implemented yet:

- Authentication
- Domain entities
- Attendance workflows
- QR code generation/scanning
- Role dashboards
