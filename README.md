# financial-management-api

Spring Boot financial management API.

## Requirements

- Docker & Docker Compose
- Java 25 (for local development)

## Getting started

```bash
cp .env.example .env
# Fill in your credentials in .env
docker compose up --build
```

The app will be available at `http://localhost:${APP_PORT}`.

## Modules

| Module | Description |
|--------|-------------|
| `financemanagement-ms1` | Core finance management microservice |
