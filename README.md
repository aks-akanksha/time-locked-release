# Time-Locked Release

A tiny end-to-end system to **create**, **schedule**, **approve**, and **execute** releases.
Backend: Spring Boot + MySQL + JWT + Flyway + OpenAPI.
Frontend: Vite + React.
```THIS IS JUST THE FIRST BASIC VERSION, THE PROJECT IS STILL IN PROGRESS AND THERE IS STILL A LOT TO IMPLEMENT!!!```

---

## Features

* Create a release (title, description, optional JSON payload)
* Schedule with a UTC timestamp
* Approve (separate role)
* Execute only when:

  * status is `APPROVED`, and
  * `scheduledAt <= now`
* Background job auto-executes APPROVED & due releases
* Optional webhook fired on execute

---

## Monorepo layout

```
.
├── demo/                 # Spring Boot backend
│   ├── src/main/java     # controllers, security, services, etc.
│   ├── src/main/resources
│   │   └── db/migration  # Flyway SQL migrations (V1..)
│   └── src/test/java     # unit & integration tests (Testcontainers)
├── release-ui/           # Vite + React UI
├── docker/               # docker-compose + UI Dockerfile
├── Dockerfile            # backend Dockerfile (multi-stage)
└── postman/collection.json
```

---

## Run it (recommended: Docker)

```bash
cd docker
docker compose up -d --build
```

* API: **[http://localhost:8081](http://localhost:8081)**
* Swagger UI: **[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)**
* Web UI (React): **[http://localhost:5173](http://localhost:5173)**

> `docker-compose.yml` starts MySQL, the backend, and a static Nginx serving the built UI.

---

## Run locally (backend + UI)

### 1) Start MySQL (via compose)

```bash
cd docker
docker compose up -d db
```

### 2) Backend

Requirements: JDK 21, Maven wrapper.

```bash
cd demo
# required JWT settings
export JWT_SECRET='8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0='
export JWT_ISSUER='example.com'

./mvnw spring-boot:run
# API now on http://localhost:8081
```

### 3) Frontend

Requirements: Node 18+ / npm.

```bash
cd release-ui
npm ci
npm run dev
# UI on http://localhost:5173
```

> The UI points to `http://localhost:8081` by default.
> To override, set `VITE_API_URL` before `npm run dev` or at build time.

---

## Login (seeded users)

On first boot, these users are seeded:

| Role     | Email                  | Password      | Can do                                         |
| -------- | ---------------------- | ------------- | ---------------------------------------------- |
| ADMIN    | `admin@example.com`    | `admin123`    | schedule, execute, approve (admin is superset) |
| APPROVER | `approver@example.com` | `approver123` | approve                                        |
| USER     | `user@example.com`     | `user123`     | create/list                                    |

You can log in from the UI (top left) or via Swagger `POST /auth/login`.

---

## Release workflow (what to click)

1. **Create** a release (USER or above)
2. **Schedule** a time (ADMIN)

   * UI converts your local time to UTC automatically
3. **Approve** (APPROVER or ADMIN)
4. **Execute** (ADMIN)

   * If you try early → **409** `Cannot execute before scheduledAt`
   * If not approved → **409** `Release must be APPROVED before execution`
5. Or let the background jobs auto-execute when due:

   * `DueExecutor` runs every 5s
   * `ReleaseScheduler` runs every 30s

---

## Webhook on execute (optional)

When a release executes, the backend can POST to a webhook.

**Configure it:**

* In `demo/src/main/resources/application.yml`:

  ```yaml
  app:
    release:
      webhook-url: "https://webhook.site/<your-id>"
  ```

* Or via env var:

  ```bash
  export APP_RELEASE_WEBHOOK_URL="https://webhook.site/<your-id>"
  ```

**Payload sent:**

```json
{
  "releaseId": 123,
  "title": "Demo",
  "payload": "{...payloadJson...}"
}
```

Open your webhook URL to see requests arrive.

---

## API (Swagger)

* Swagger UI: `http://localhost:8081/swagger-ui/index.html`

Key endpoints:

* `POST /auth/login` → `{ token }`
* `GET /api/v1/releases`
* `POST /api/v1/releases` → create (`{ title, description, payloadJson }`)
* `POST /api/v1/releases/{id}/actions/schedule` → `{ scheduledAt: "2025-10-05T11:25:00Z" }`
* `POST /api/v1/releases/{id}/actions/approve`
* `POST /api/v1/releases/{id}/actions/execute`

> Business rule violations return **409 Conflict**.

---

## Time zones

* DB & API store/use **UTC**
* UI takes your **local** time and sends **UTC ISO** (`.toISOString()`)
* If calling the API yourself, send UTC like `2025-10-05T11:25:00Z`

---

## Environment variables (backend)

| Name                      | Required                             | Example                                        | Notes             |
| ------------------------- | ------------------------------------ | ---------------------------------------------- | ----------------- |
| `JWT_SECRET`              | yes                                  | `8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0=` | 32+ bytes Base64  |
| `JWT_ISSUER`              | yes                                  | `example.com`                                  | Must match config |
| `APP_RELEASE_WEBHOOK_URL` | no                                   | `https://webhook.site/<id>`                    | Optional          |
| `SPRING_DATASOURCE_*`     | if you’re not using compose defaults | see `docker/docker-compose.yml`                | JDBC settings     |

---

## Tests

* Unit tests (service, with Mockito):

  ```bash
  cd demo
  ./mvnw -Dtest=ReleaseServiceTest test
  ```

* All tests (includes Testcontainers MySQL integration tests):

  ```bash
  cd demo
  ./mvnw test
  ```

---

## Troubleshooting

* **401 Unauthorized** → login again and paste Bearer token in Swagger **Authorize**
* **403 Forbidden** → you don’t have the required role (see table above)
* **409 Conflict** → business rule (approve first; or wait until `scheduledAt`)
* **Webhook not received** → set `app.release.webhook-url` (or env), and ensure outbound internet is allowed

---

## License

MIT (or your preferred license)

---
