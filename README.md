# LexFlow

LexFlow is a backend-focused legal case management application built with Java and Spring Boot.

It helps manage:
- legal cases
- deadlines
- notes
- documents

The project demonstrates a structured Spring Boot application with PostgreSQL, Flyway migrations, Docker support, testing, and CI.

---

## Features

- Manage legal cases
- Track deadlines with priority and completion status
- Add notes to cases
- Upload, download, and delete documents
- Search and filter cases
- Dashboard with case and deadline overview
- PostgreSQL persistence
- Flyway-based database schema management
- Demo seed data for quick startup
- Dockerized application setup
- Automated tests with JUnit and Mockito
- CI with GitHub Actions

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- Flyway
- Maven
- Docker / Docker Compose
- JUnit 5
- Mockito
- GitHub Actions
- Spring Security
- springdoc-openapi (Swagger UI)

---

## Project Structure

```text
src
├── main
│   ├── java/com/lexflow
│   │   ├── case_
│   │   ├── common
│   │   ├── deadline
│   │   ├── document
│   │   └── note
│   └── resources
│       ├── db/migration
│       │   ├── V1__init_schema.sql
│       │   └── V2__seed_demo_data.sql
│       ├── templates
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-docker.yml
└── test
```

---

## REST API

Interactive documentation is available at **http://localhost:8080/swagger-ui.html** (raw spec: `/v3/api-docs`).
Click **Authorize** and log in with one of the demo users:

| User | Password | Access |
|------|----------|--------|
| `user` | `user123` | read-only (`GET`) |
| `admin` | `admin123` | read and write |

The API uses HTTP Basic authentication, is stateless, and returns errors as
[RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) problem details (`application/problem+json`).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cases` | Search cases (`keyword`, `status`, `sort`, `page`, `size`) |
| GET | `/api/v1/cases/{id}` | Get a case |
| POST | `/api/v1/cases` | Create a case |
| PUT | `/api/v1/cases/{id}` | Update a case |
| DELETE | `/api/v1/cases/{id}` | Delete a case with its deadlines, notes and documents |
| GET | `/api/v1/cases/{caseId}/deadlines` | List deadlines of a case |
| POST | `/api/v1/cases/{caseId}/deadlines` | Add a deadline |
| GET | `/api/v1/deadlines/overdue` | Open deadlines past their due date |
| GET | `/api/v1/deadlines/upcoming` | Open deadlines due today or later |
| GET / PUT / DELETE | `/api/v1/deadlines/{id}` | Read, update, delete a deadline |
| PATCH | `/api/v1/deadlines/{id}/complete` | Mark a deadline as completed |
| GET | `/api/v1/cases/{caseId}/notes` | List notes of a case |
| POST | `/api/v1/cases/{caseId}/notes` | Add a note |
| GET / PUT / DELETE | `/api/v1/notes/{id}` | Read, update, delete a note |

Example:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/v1/cases \
  -H "Content-Type: application/json" \
  -d '{"title":"Lease termination","client":"Globex","type":"CONTRACT","status":"OPEN"}'
```

