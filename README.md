# Financial Management API

Financial management microservices project developed with Spring Boot.

## Current Module

| Module | Description |
|---|---|
| `financemanagement-ms1` | User management API |

## Requirements

- Docker and Docker Compose
- Java 25 for local development

## Run with Docker

```bash
cp .env.example .env
# Configure the values in .env
docker compose up --build
```text

The API will be available at:

```text
http://localhost:<APP_PORT>
```text

## API Documentation

Swagger UI:

```text
http://localhost:<APP_PORT>/swagger-ui/index.html
```text

OpenAPI JSON:

```text
http://localhost:<APP_PORT>/v3/api-docs
```text

## User Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Create a user |
| `GET` | `/users` | List users |
| `GET` | `/users/{id}` | Find a user by ID |
| `PUT` | `/users/{id}` | Update a user |
| `DELETE` | `/users/{id}` | Delete a user |

## Validation and Error Handling

The API validates request fields using Bean Validation and returns standardized errors.

Common status codes:

| Status | Description |
|---:|---|
| `201` | User created |
| `200` | Request completed successfully |
| `204` | User deleted |
| `400` | Invalid request data |
| `404` | User not found |
| `409` | Email or username already exists |

## Tests

Run unit tests locally:

```bash
cd financemanagement-ms1
./mvnw test
```text

The current unit tests use JUnit and Mockito to validate the `UserService` business rules.
