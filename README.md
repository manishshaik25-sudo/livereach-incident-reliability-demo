# Reliable Camera Event & Incident API

**Independent portfolio demonstration inspired by the general problem space of cloud video security and incident management. Not affiliated with LiveReach AI, and not an implementation of its private systems.**

## Why this project?
A camera or edge device may retry an event after a network timeout. Without deduplication, the backend could create multiple incident records for one event. Reviewers may also update an incident simultaneously. This small API explores those correctness problems using Java 21, Spring Boot, JPA and MySQL.

### Capabilities
- `POST /api/events`: ingest a synthetic event with an **eventId**; returns `201` on creation or `200` on identical delivery replay
- A reused eventId with a different payload gets `409 Conflict`
- `GET /api/incidents/{id}`: fetch a record
- `PATCH /api/incidents/{id}/status`: update using an `expectedVersion`; stale versions get `409 Conflict`
- Unique database key on event ID; JPA `@Version` on incidents; event + incident saved in one transaction

## Architecture
```text
Simulated camera client --> POST /api/events --> IncidentService
                                              |-- camera_events(eventId PK, payload hash)
                                              |-- incidents(eventId UNIQUE, version)
Review client ----------> PATCH /api/incidents/{id}/status
```

## Setup (Windows PowerShell)
Prerequisites: JDK 21, Maven, Docker Desktop. MySQL uses **port 3307**, avoiding conflict with other local MySQL projects.

```powershell
docker compose up -d
mvn test
mvn -Dtest=MysqlContainerIT test  # requires Docker, exercises MySQL through Testcontainers
mvn spring-boot:run
```

## Try it
```powershell
$body = '{"eventId":"camera-event-1001","siteId":"site-A","cameraId":"camera-12","eventType":"MOTION","observedAt":"2026-11-17T12:00:00Z"}'
$first = Invoke-RestMethod -Uri http://localhost:8080/api/events -Method Post -ContentType application/json -Body $body
$first
# Repeat exactly; expects the original incident ID rather than a second incident
Invoke-RestMethod -Uri http://localhost:8080/api/events -Method Post -ContentType application/json -Body $body
# Update status using the version from $first
$statusBody = @{status='REVIEWING';expectedVersion=$first.version} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/incidents/$($first.id)/status" -Method Patch -ContentType application/json -Body $statusBody
```

## Testing
- `IncidentIntegrationTest`: identical replay, mismatched payload, stale version (H2 with MySQL compatibility mode)
- `MysqlContainerIT`: real MySQL duplicate-delivery test (explicit command above)

**Do not claim tests passed until you run them on your machine.** `MysqlContainerIT` uses the `IT` suffix, so it is explicitly invoked above instead of being included in the default `mvn test` run.

## Tradeoffs / limits
- Event ID is the idempotency key; in production it should come from a trusted event producer or be scoped to a tenant/camera, with payload validation and authentication.
- An event and its incident are written in one transaction. A duplicate with mismatched content is rejected.
- The unique database constraint handles competing inserts, but a high-throughput production system would benefit from deliberate retry/backoff, isolation-level testing and metrics for transient transaction failures.
- `@Version` protects updates against lost writes; this demo also requires `expectedVersion` from the client.
- This demo does **not** ingest or analyze video, run object detection, identify people, store sensitive footage, deliver alerts, enforce access controls or integrate with LiveReach APIs.
- For production: add authentication/authorization, pagination, audit trail, event queue/outbox, privacy controls, tracing, dead-letter handling, multi-tenant isolation, retention/deletion policy, and load testing.

## Contact context
The project's inspiration is the *generic* need for reliable event processing in multi-site video systems; it is **not** evidence of a defect in any company's products.

See [ENGINEERING_NOTE.md](ENGINEERING_NOTE.md) and [GITHUB_GUIDE.md](GITHUB_GUIDE.md).
