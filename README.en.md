# Example REST Backend

This project is a Spring Boot REST API server. It uses MySQL and JWT auth, and can be run with Docker Compose.

## 1) Tech Stack

- Spring Boot 3.4.2
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT (jjwt 0.12.5)
- Docker / Docker Compose
- Java 17

## 2) Key Features

- Signup/Login/Refresh token issuance
- Separate Access/Refresh JWT
- HttpOnly cookie for refresh token
- CORS allowed origins for frontend

## 3) Run (Docker)

```sh
docker compose up --build
```

Default ports:

- API: <http://localhost:8080>
- MySQL: localhost:3306

## 4) Environment Variables

JWT settings are managed in `.env`.

- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION_MS`
- `JWT_REFRESH_EXPIRATION_MS`

## 5) Main APIs

- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/auth/refresh`