# BlackRock Hacking India 2026 - Auto-Saving API for Planned Retirement

This project implements the required endpoints from the challenge PDF and adds:
- **JWT authentication** (`/auth/login`)
- **Scalable design patterns**: Pipeline/Chain-of-Responsibility, Strategy+Factory, Specification validation, Idempotency
- **CQRS-lite**: Command-side (`CommandFacade`) for parse/validate/filter and Query-side (`QueryFacade`) for returns/performance
- **Resilience**: RateLimiter + Bulkhead + TimeLimiter on returns endpoints (Resilience4j)
- **MDC correlation-id logging** via `X-Correlation-Id`

## Run
```bash
mvn clean package -DskipTests
java -jar target/arbor-thrive-1.0.0.jar

```
Runs on `http://localhost:5477`

## Docker
```bash
mvn clean package
docker build --no-cache -t arbor-thrive-api
docker run --name arbor-thrive-api -p 5478:5477 arbor-thrive-api
```

## Auth (JWT)
Login:
`POST /blackrock/challenge/v1/auth/login`

Default credentials (change in `application.yml`):
- username: `demo`
- password: `demo123`

Use the token:
`Authorization: Bearer <token>`

## Correlation ID (MDC)
Send optional header:
`X-Correlation-Id: <any-id>`
If absent, server generates one and returns it in the response header.

## Idempotency (returns endpoints)
Send optional header:
`Idempotency-Key: <unique-key>`
Same key + same endpoint => returns cached response.

## Required Endpoints
- POST `/blackrock/challenge/v1/transactions:parse`
- POST `/blackrock/challenge/v1/transactions:validator`
- POST `/blackrock/challenge/v1/transactions:filter`
- POST `/blackrock/challenge/v1/returns:nps`
- POST `/blackrock/challenge/v1/returns:index`
- GET  `/blackrock/challenge/v1/performance`

## Algorithmic optimizations (time & space)
Optimized the core q/p/k logic for best practical complexity:

- **Parse:** O(n log n) sort by time (timestamps parsed once)
- **Validate:** O(n) with HashSet duplicate detection
- **Q periods (override):** sweep-line + max-heap of active intervals
  - Time **O((n+q) log q)** instead of O(n·q)
- **P periods (extra add):** sweep-line with start/end events
  - Time **O(p log p + n)** instead of O(n·p)
- **K aggregation:** prefix sums + binary search
  - Time **O(n + k log n)**
- **Filter K-membership:** merge K intervals + single scan
  - Time **O(k log k + n)**

These improvements keep the API fast even when n/q/p/k are large.
