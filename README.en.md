# Example REST Backend

This project is a Spring Boot REST API server. It uses MySQL and JWT auth, and can be run with Docker Compose. AI question generation uses Ollama.

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
- AI question generation/session/grade/wrong-note APIs

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

Additional config (defaults in `application.yml`):

- `FRONTEND_ORIGIN` (default: <http://localhost:5173>)
- `OLLAMA_BASE_URL` (default: <http://host.docker.internal:11434>)
- `OLLAMA_MODEL` (default: `llama3.1:8b-instruct-q4_0`)

## 5) Main APIs

- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/auth/refresh`
- POST `/api/exam-ai/session`
- POST `/api/exam-ai/grade`
- GET `/api/exam-ai/wrong`

## 6) Ollama Setup

AI question generation expects a local Ollama server.

- Install Ollama and run the server
- Example model pull: `ollama pull llama3.1:8b-instruct-q4_0`
