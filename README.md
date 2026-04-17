# LexFlow

LexFlow is a Spring Boot web application for managing legal cases, deadlines, notes, and documents.

It was built as a backend-focused pet project to demonstrate:
- Java and Spring Boot development
- layered application design
- CRUD workflows
- database integration with PostgreSQL
- testing with JUnit and Mockito
- CI with GitHub Actions
- containerized local setup with Docker

---

## Features

### Case management
- create legal cases
- edit and delete cases
- search cases by keyword
- filter by status
- sort and paginate results

### Deadline tracking
- add deadlines to cases
- mark deadlines as completed
- view overdue deadlines
- view upcoming deadlines

### Notes
- add notes to cases
- store case-related internal context
- delete notes

### Documents
- upload documents for cases
- download uploaded files
- delete documents

### Dashboard
- total cases
- open cases
- overdue deadlines
- upcoming deadlines
- recent cases
- quick actions for common workflows

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- H2 (for repository tests)
- JUnit 5
- Mockito
- GitHub Actions
- Docker
- Docker Compose

---

## Project Structure

```text
src/main/java/com/lexflow
├── case_
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
├── common
│   └── exception
├── deadline
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
├── document
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
├── note
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
└── LexFlowApplication