# BookHive

An online book club and reading tracker. Members start or join clubs, pick a book, log their reading progress, and discuss each chapter. Discussion is scoped per chapter, so people who are behind never see spoilers.

## Features

- **Accounts**: sign up and log in with JWT authentication; global roles (ADMIN / MEMBER) and per-club roles (club admin / member)
- **Clubs**: create, join, and delete clubs. Only the creator can delete a club; only club admins can set the current book
- **Book catalog**: admins add books, with search and autofill from the Google Books API. Anyone can browse, read details, and review
- **Discover**: browse the external Google Books catalog by keyword, author, or category
- **Reading progress**: log your chapter and see every club member's progress bar
- **Chapter discussions**: comment per chapter
- **Reviews**: 1-5 star ratings with text, one review per user per book
- **Notifications**: reply, new-book, and new-member alerts with an unread badge, mark-as-read, and dismiss
- **Pagination**: clubs, books, comments, and reviews

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.1.1, Java 21, Spring Security + JWT, Spring Data JPA |
| Database | PostgreSQL 16 |
| Cache | Redis (book catalog and book detail, 10 minute TTL) |
| Messaging | Apache Kafka (KRaft mode, single broker) |
| Frontend | React (Vite), Tailwind CSS v4, shadcn/ui |
| Testing | JUnit 5, Mockito, Testcontainers |
| Packaging | Docker, Docker Compose, nginx |

## Architecture notes

- **Async notifications**: when someone comments, reviews, joins a club, or a club changes its book, the service publishes a `NotificationEvent` to the Kafka topic `notifications`. A consumer reads the event and writes the notification row. The request that triggered it does not wait for the notification to be created.
- **Caching**: `GET /api/books` and `GET /api/books/{id}` are cached in Redis and evicted when a book is created or deleted.
- **Notification delivery**: the frontend polls the unread count every 30 seconds. Kafka only changes how rows are created, not how the frontend reads them.
- **Permissions are enforced on the backend**: the UI hides admin-only controls, but every protected endpoint re-checks roles against the database.

## Run with Docker

Requires Docker and Docker Compose.

1. Copy `.env.example` to `.env` and fill in your Google Books API key:
```
   GOOGLE_BOOKS_API_KEY=your_google_books_api_key
```
2. Make sure ports 5173, 8082, 5432, 6379, and 9092 are free (stop any locally running backend or frontend).
3. Start everything:
```bash
   docker-compose up --build
```
4. Open http://localhost:5173. The API is at http://localhost:8082/api.

Stop with `Ctrl+C`. `docker-compose down -v` also deletes the database volume.

## Run locally (app outside Docker)

Start only the infrastructure:

```bash
docker-compose up postgres redis kafka
```

Then run the backend and frontend:

```bash
# backend (Java 21, Maven)
cd BookhiveBackend
export GOOGLE_BOOKS_API_KEY=your_key
mvn spring-boot:run

# frontend (Node 20)
cd bookehivefrontend
npm install
npm run dev
```

The backend defaults to `localhost` for Postgres, Redis, and Kafka, so no extra configuration is needed. Kafka advertises `localhost:9092` to your machine and `kafka:19092` to other containers.

## Tests

```bash
cd BookhiveBackend
mvn test
```

Unit tests (Mockito) cover the book and review business rules. An integration test boots the full application against a throwaway Postgres container using Testcontainers, so Docker must be running.

## Configuration

All settings can be overridden with environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | 8082 | Backend port |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/bookhive | Database URL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | postgres / pass | Database credentials |
| `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` | localhost / 6379 | Redis |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka |
| `GOOGLE_BOOKS_API_KEY` | (empty) | Google Books search |

## API overview

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/signup`, `POST /api/auth/login` |
| Books | `GET /api/books`, `GET /api/books/paginated`, `GET /api/books/search`, `GET /api/books/{id}`, `POST /api/books`, `DELETE /api/books/{id}` |
| Reviews | `GET/POST /api/books/{bookId}/reviews` |
| Clubs | `GET/POST /api/clubs`, `GET /api/clubs/grouped`, `GET /api/clubs/grouped/paged`, `GET /api/clubs/{id}`, `DELETE /api/clubs/{id}`, `POST /api/clubs/{id}/join`, `PUT /api/clubs/{id}/current-book/{bookId}`, `GET /api/clubs/reading/{bookId}` |
| Progress | `GET /api/clubs/{id}/progress`, `GET/PUT /api/clubs/{id}/progress/me` |
| Comments | `GET/POST /api/clubs/{clubId}/comments?chapter=` |
| Notifications | `GET /api/notifications/me`, `GET /api/notifications/me/unread-count`, `PUT /api/notifications/{id}/read`, `DELETE /api/notifications/{id}` |

All endpoints except auth require a `Bearer` token.

## Project structure

```
Major-Project/
├── docker-compose.yml
├── .env.example
├── BookhiveBackend/        Spring Boot API (entity, repository, service, controller, security, config)
└── bookehivefrontend/      React app (pages, components, context, api)
```

## Live demo

_Add the deployed URL here once deployed._