# LexFlow

LexFlow is a Spring Boot web application for managing legal cases, deadlines, notes, and documents.

It is designed as a portfolio project that demonstrates:
- CRUD workflows
- Spring Boot MVC architecture
- PostgreSQL integration
- Thymeleaf server-side rendering
- file upload and download
- filtering, sorting, and pagination

## Features

### Case management
- create, view, edit, and delete legal cases
- track client, case type, and status
- browse cases with:
    - search by title or client
    - status filter
    - sorting
    - pagination

### Deadline tracking
- add deadlines to a case
- edit deadlines
- mark deadlines as completed
- delete deadlines
- view overdue deadlines

### Notes
- add notes to a case
- edit notes
- delete notes

### Document management
- upload real files
- store file metadata in PostgreSQL
- save uploaded files locally
- download uploaded files
- delete uploaded files
- validate file type
- validate file size
- show upload timestamp and formatted file size

## Tech Stack

- Java 20
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Thymeleaf
- Bootstrap 5
- Maven

## Project Structure

```text
src/main/java/com/lexflow
├── case_
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
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
└── common
    └── controller