# springboot-task-api

A production-style Task Management REST API built with **Java 17, Spring Boot 3, Spring Data JPA, Spring Security (JWT), PostgreSQL, and JUnit 5**.

> **Portfolio project — built October 2026.** This is a personal portfolio project demonstrating backend API development. It is not prior work experience, not a client project, and has no production users.

## What it does

- User registration and login with BCrypt password hashing and JWT authentication.
- Role-based access: `USER` (default) and `ADMIN` (project deletion is admin-only).
- Full CRUD for **projects** and **tasks**.
- Tasks support status (`TODO`, `IN_PROGRESS`, `DONE`), priority (`LOW`, `MEDIUM`, `HIGH`), due dates, and assignment to a project.
- Task listing with **pagination, filtering by status/priority, and title/description search**.
- Bean Validation on every request, plus a global exception handler returning consistent JSON errors (`400 / 401 / 403 / 404 / 409 / 500`).
- Users can only see and modify their own projects and tasks.

## Architecture

```text
Controller (REST + validation)
  -> Service (business rules, ownership checks)
    -> Repository (Spring Data JPA + Specifications)
      -> PostgreSQL (prod) / H2 (tests)

Security: JwtAuthFilter -> Spring Security filter chain (stateless, BCrypt)
```

| Layer | Key files |
|---|---|
| Controllers | `AuthController`, `ProjectController`, `TaskController` |
| Services | `AuthService`, `ProjectService`, `TaskService` |
| Security | `SecurityConfig`, `JwtService`, `JwtAuthFilter` |
| Persistence | `User`, `Project`, `Task` entities + Spring Data repositories |
| Errors | `GlobalExceptionHandler`, `ResourceNotFoundException`, `ConflictException` |

## Run it

### Option A — Docker (recommended)

```bash
docker compose up --build
# API at http://localhost:8080
```

### Option B — Local with PostgreSQL

```bash
# Start PostgreSQL and create a database, then:
export DB_URL=jdbc:postgresql://localhost:5432/taskdb
export DB_USERNAME=taskuser
export DB_PASSWORD=taskpass
export JWT_SECRET=<a-long-random-secret-at-least-32-chars>
./mvnw spring-boot:run   # or: mvn spring-boot:run
```

Requires Java 17 and Maven. The `JWT_SECRET` in `application.properties` is a **dev-only default** — override it with `JWT_SECRET` in any real deployment. Never commit real secrets.

## API

Base URL: `http://localhost:8080`

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a user, returns JWT |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Projects

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/projects` | JWT | Create a project |
| GET | `/api/projects` | JWT | List my projects |
| GET | `/api/projects/{id}` | JWT | Get one project |
| PUT | `/api/projects/{id}` | JWT | Update a project |
| DELETE | `/api/projects/{id}` | ADMIN | Delete a project |

### Tasks

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/tasks` | JWT | Create a task |
| GET | `/api/tasks` | JWT | List my tasks (paginated, filterable) |
| GET | `/api/tasks/{id}` | JWT | Get one task |
| PUT | `/api/tasks/{id}` | JWT | Update a task |
| DELETE | `/api/tasks/{id}` | JWT | Delete a task |

List query params: `page`, `size`, `sort` (e.g. `sort=createdAt,desc`), `status`, `priority`, `search`.

### curl examples

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","email":"demo@example.com","password":"password123"}'

# Save the token
TOKEN=<token-from-register-response>

# Create a project
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Launch API","description":"Ship the task API"}'

# Create a task (use the project id returned above)
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Write integration tests","status":"IN_PROGRESS","priority":"HIGH","projectId":1}'

# List with filter + pagination
curl "http://localhost:8080/api/tasks?status=TODO&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Update
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Write integration tests","status":"DONE","priority":"HIGH","projectId":1}'

# Delete
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

## Tests

```bash
mvn test
```

Tests use an H2 in-memory database (`src/test/resources/application-test.properties`), so no PostgreSQL is needed to run them.

| Test class | Type | Covers |
|---|---|---|
| `TaskServiceTest` | Unit (Mockito) | Default status/priority, project resolution, not-found, delete |
| `AuthIntegrationTest` | Integration (MockMvc) | Register → login → authenticated call, duplicate register → 409, unauthenticated → 401, invalid input → 400 |
| `TaskControllerIntegrationTest` | Integration (MockMvc) | Full task lifecycle, project linkage, pagination, status filter, search, validation, not-found, delete, cross-user isolation |

## Limitations

- No refresh tokens, password reset, or email verification.
- No rate limiting or audit logging.
- Project deletion is admin-only by design (users can still delete their own tasks).
- No frontend — API only.
- Testcontainers-style Postgres integration tests are not included; integration tests run against H2.
