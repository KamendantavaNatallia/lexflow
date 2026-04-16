# LexFlow

LexFlow is a legal case management application built with Java and Spring Boot.

It was created as a portfolio project to demonstrate backend engineering skills through a realistic legal-tech domain: case management, deadline tracking, notes, document workflows, validation, centralized exception handling, layered architecture, and automated testing.

## Why this project

LexFlow simulates an internal system that could be used by a law office or legal operations team to:

- manage legal cases
- track important deadlines
- organize case notes
- upload and manage case-related documents
- browse records with filtering, sorting, and pagination

The goal of the project is not only to build a functional CRUD application, but also to model realistic backend workflows in a business domain.

---

## Core Features

### Case management
- create, edit, view, and delete legal cases
- track case title, client, type, and status
- browse cases with:
  - keyword search
  - status filtering
  - sorting
  - pagination
  - page size selection

### Deadline tracking
- add deadlines to a case
- edit deadlines
- mark deadlines as completed
- delete deadlines
- view overdue deadlines
- view upcoming deadlines on the dashboard

### Notes
- add notes to a case
- edit notes
- delete notes

### Document management
- upload files linked to legal cases
- store file metadata in PostgreSQL
- save uploaded files locally
- download uploaded files
- delete uploaded files
- validate file type and file size
- display upload timestamp and formatted file size

### Dashboard
- total cases
- open cases
- overdue deadlines
- upcoming deadlines
- recent cases
- quick actions

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Thymeleaf
- Bootstrap 5
- Maven

### Testing
- JUnit 5
- Mockito
- MockMvc
- H2

---

## Architecture

LexFlow uses a layered backend structure with feature-based packaging.

Each domain module contains:
- controller
- model
- repository
- service

This keeps the codebase modular and easier to extend as the project grows.

### Main source set

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
    └── exception