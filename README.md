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