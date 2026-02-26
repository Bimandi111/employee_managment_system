# Employee Management System

A full-stack CRUD web application for managing company employee records, 
built as part of the Advanced Java Programming module at the 
Java Institute for Advanced Technology.

## Tech Stack

- **Backend:** Java 17, Jersey 3 (JAX-RS), Hibernate 7 ORM
- **Database:** MySQL 8 — 5 tables with FK constraints
- **Auth:** JWT (JJWT 0.11.5) — stateless token-based authentication
- **Server:** Embedded Apache Tomcat 11 — runs as a standalone fat JAR
- **Build:** Apache Maven 3 with Shade plugin
- **Frontend:** Vanilla JS Single Page Application (SPA)

## Features

- Full CRUD on employee records (create, read, update, delete)
- Search and filter by name, department, position, and hire date
- Role-based access control — ADMIN, HR, and VIEWER roles
- Soft-delete archive — removed employees saved to past_employees table
- JWT authentication on all API endpoints
- RESTful API with proper HTTP methods and status codes
- Runs as a single executable JAR — no external server needed

## Getting Started

### Prerequisites
- JDK 17+
- Maven 3.6+
- MySQL 8.0+


## Project Structure
```
src/main/java/com/EMS/
├── entity/      JPA entities (Employee, Department, Position, User, PastEmployee)
├── dao/         Data access layer — native JDBC via Hibernate session
├── service/     Business logic and validation
├── resource/    Jersey REST controllers
├── auth/        JWT filter, token utility, CORS, security headers
└── util/        HibernateUtil, ApiResponse wrapper
```

java  jersey  hibernate  mysql  jwt  rest-api  tomcat  maven  crud  spa
