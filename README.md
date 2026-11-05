# Financial Management API

Financial management microservices project developed with Spring Boot.

## Modules

| Module | Description |
|---|---|
| `financemanagement-ms1` | User management, authentication, login, and JWT issuance API |
| `financemanagement-ms2` | Financial management microservice — initial setup |

## Requirements

- Docker and Docker Compose
- Java 25 for local development

## Environment Configuration

Before running the project, create your local environment file:

```bash
cp .env.example .env
```text

Configure the values in `.env` before starting the containers.

### JWT Secret

MS1 uses `JWT_SECRET` to sign and validate JWT tokens during authentication.

`JWT_SECRET` must be a Base64-encoded key with at least 256 bits (32 bytes). If the value is invalid, not Base64, or shorter than 256 bits, MS1 will not start.

Generate a valid secret with:

```bash
openssl rand -base64 32
```text

Do not commit the `.env` file or expose the JWT secret.

MS2 does not generate or validate JWT tokens, so it does not receive `JWT_SECRET` or `JWT_EXPIRATION_MS` through Docker Compose.

## Run with Docker

```bash
docker compose up --build
```text

The services will be available at:

| Service | URL |
|---|---|
| MS1 | `http://localhost:<MS1_APP_PORT>` |
| MS2 | `http://localhost:<MS2_APP_PORT>` |

## Authentication Flow

1. The client authenticates with MS1.
2. MS1 validates the user credentials and returns a JWT token.
3. The client sends the JWT in the `Authorization` header when accessing protected resources.

Example header:

```text
Authorization: Bearer <jwt-token>
```text

MS1 is responsible for generating and validating the JWT.

## Internal Service Communication

Docker Compose creates an internal network for the services.

MS2 receives the MS1 base URL through the `MS1_URL` environment variable:

```text
MS1_URL=http://ms1:8080
```text

Inside the Docker Compose network, MS2 can access MS1 through:

```text
http://ms1:8080
```text

The hostname `ms1` is available only between Docker containers. From the host machine, MS1 and MS2 must be accessed through `localhost` and their respective exposed ports.

When MS2 needs to call MS1 on behalf of a client, it can forward the received `Authorization` header to MS1. MS1 remains responsible for validating the JWT.

## API Documentation

### MS1

Swagger UI:

```text
http://localhost:<MS1_APP_PORT>/swagger-ui/index.html
```text

OpenAPI JSON:

```text
http://localhost:<MS1_APP_PORT>/v3/api-docs
```text

## MS1 User Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Create a user |
| `GET` | `/users` | List users |
| `GET` | `/users/{id}` | Find a user by ID |
| `PUT` | `/users/{id}` | Update a user |
| `DELETE` | `/users/{id}` | Delete a user |

## Validation and Error Handling

MS1 validates request fields using Bean Validation and returns standardized errors.

Common status codes:

| Status | Description |
|---:|---|
| `201` | User created |
| `200` | Request completed successfully |
| `204` | User deleted |
| `400` | Invalid request data |
| `401` | Invalid or expired authentication token |
| `404` | User not found |
| `409` | Email or username already exists |

## Tests

Run MS1 unit tests locally:

```bash
cd financemanagement-ms1
./mvnw test
```text

The current MS1 unit tests use JUnit and Mockito to validate the `UserService` business rules.
