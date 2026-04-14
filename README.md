# LexFlow

LexFlow is a legal case management application built with Java and Spring Boot.

It was created as a portfolio project to demonstrate backend engineering skills through a realistic legal-tech domain: case management, deadline tracking, notes, and document workflows.

## Why this project

LexFlow simulates an internal system that could be used by a law office or legal operations team to:

* manage legal cases
* track important deadlines
* organize case notes
* upload and manage case-related documents
* browse records with filtering, sorting, and pagination

The goal of the project is not only to build a functional application, but also to model realistic backend workflows in a business domain.

## Core Features

### Case management

* create, edit, view, and delete legal cases
* track case title, client, type, and status
* browse cases with:

  * keyword search
  * status filtering
  * sorting
  * pagination

### Deadline tracking

* add deadlines to a case
* edit deadlines
* mark deadlines as completed
* delete deadlines
* view overdue deadlines

### Notes

* add notes to a case
* edit notes
* delete notes

### Document management

* upload files linked to legal cases
* store file metadata in PostgreSQL
* save uploaded files locally
* download uploaded files
* delete uploaded files
* validate file type and file size
* display upload timestamp and formatted file size

## Tech Stack

* Java 17
* Spring Boot 3
* Spring MVC
* Spring Data JPA
* Hibernate
* PostgreSQL
* Thymeleaf
* Bootstrap 5
* Maven

## Architecture

The project is organized by feature modules:

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
```

This structure keeps business domains separated and makes the codebase easier to scale and maintain.

## Running locally

### Prerequisites

* Java 17
* Maven
* PostgreSQL

### Steps

1. Clone the repository
2. Create a PostgreSQL database
3. Update database credentials in configuration
4. Run the application:

```bash
mvn spring-boot:run
```

5. Open in browser:

```text
http://localhost:8080
```

## Engineering Highlights

This project demonstrates:

* modular feature-based package organization
* layered architecture with controller, service, repository, and model layers
* relational domain modeling with interconnected entities
* business workflows around cases, deadlines, notes, and documents
* practical backend features such as validation, filtering, pagination, and file handling

## Current Focus

This project is being improved to become a stronger backend portfolio project.

Planned improvements include:

* REST API layer
* global exception handling
* database migrations
* Docker support
* automated tests
* CI pipeline
* authentication and role-based access control

## Resume Positioning

LexFlow demonstrates:

* backend application design with Spring Boot
* relational data modeling with JPA and PostgreSQL
* layered architecture by feature
* server-side validation and business workflows
* document upload and file handling
* filtering, sorting, and pagination in a realistic domain

## Future Improvements

* REST API with Swagger/OpenAPI
* Flyway or Liquibase migrations
* Docker Compose for local setup
* test coverage with integration tests
* Spring Security with roles
* cloud-compatible file storage
* audit trail for domain changes

## Screenshots

Screenshots will be added as the UI evolves.

## Author

Created as a backend portfolio project in the legal-tech domain.
