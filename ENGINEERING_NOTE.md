# Engineering note — reliable event-to-incident processing

## Problem pattern
In distributed event delivery, retries can occur after timeouts even when the server committed a write. Two copies of the same event should not create two incident cases. Concurrent human review of an incident can also overwrite a newer state if updates do not check version information.

## Approach
1. Accept a caller-provided eventId alongside a synthetic camera/site/eventType and timestamp.
2. Store the eventId as a primary key and the canonical payload hash with it. Save the incident in the same database transaction.
3. Repeated identical deliveries return the linked incident; reuse of an eventId with different data returns 409.
4. Maintain a numeric incident version with JPA `@Version`; reviewers include expectedVersion to reject stale updates.

## Alternative: pessimistic locking
Pessimistic locking can serialize writes while holding row locks, but creates contention and potential deadlocks. Optimistic version checking is useful when most incident updates do not conflict. For heavier contention, transactions should be retried with bounded backoff and measured.

## Limits
This is a teaching/portfolio prototype. It lacks a message broker, distributed tracing, security, tenant authorization, durable delivery queue, large-scale ingestion and camera/video connectivity. The demo does not inspect LiveReach's private architecture or imply any known defect.

## What I would measure before production
Duplicate delivery rate, successful and failed creates, write contention/conflict rate, P95 latency, throughput, deadlocks, database unique-key violations, and audit integrity.
