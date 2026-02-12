# Example REST Backend

이 프로젝트는 Spring Boot 기반 REST API 서버입니다. MySQL과 JWT 인증을 사용하며 Docker Compose로 실행할 수 있습니다. AI 문제 생성은 Ollama를 사용합니다.

## 1) 기술 스택

- Spring Boot 3.4.2
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT (jjwt 0.12.5)
- Docker / Docker Compose
- Java 17

## 2) 주요 기능

- 회원가입/로그인/리프레시 토큰 발급
- Access/Refresh JWT 분리
- HttpOnly 쿠키로 리프레시 토큰 관리
- CORS 허용(프론트엔드 Origin 지정)
- AI 문제 생성/문제풀이 세션/채점/오답노트 API

## 3) 실행 방법 (Docker)

```sh
docker compose up --build
```

기본 포트:

- API: <http://localhost:8080>
- MySQL: localhost:3306

## 4) 환경 변수

`.env` 파일에서 JWT 관련 설정을 관리합니다.

- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION_MS`
- `JWT_REFRESH_EXPIRATION_MS`

추가 설정(기본값은 `application.yml` 참고):

- `FRONTEND_ORIGIN` (기본: <http://localhost:5173>)
- `OLLAMA_BASE_URL` (기본: <http://host.docker.internal:11434>)
- `OLLAMA_MODEL` (기본: `llama3.1:8b-instruct-q4_0`)

## 5) 주요 API

- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/auth/refresh`
- POST `/api/exam-ai/session`
- POST `/api/exam-ai/grade`
- GET `/api/exam-ai/wrong`

## 6) Ollama 준비

AI 문제 생성은 로컬 Ollama 실행을 전제로 합니다.

- Ollama 설치 후 서버 실행
- 모델 다운로드 예시: `ollama pull llama3.1:8b-instruct-q4_0`
