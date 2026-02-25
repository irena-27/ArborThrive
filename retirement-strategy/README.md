# ArborThrive CQRS Monorepo (Post-hackathon v1.0+)

Post-hackathon refactor of your BlackRock challenge project into a **multi-module CQRS deployment model**.

## Modules
- `shared-domain` – DTOs + utils
- `shared-observability` – MDC correlation filter + request timing
- `shared-security` – JWT service/filter + common SecurityConfig
- `shared-core` – pipelines, specs, returns calculators, idempotency + store ports
- `command-service` – transaction parse/validator/filter endpoints
- `query-service` – returns + performance endpoints

## Why this helps
- **Independent scaling** of command vs query workloads in AKS
- **Shared security logic** avoids drift between services
- **Ports/adapters** (`IdempotencyStore`, `ReadCacheStore`, `CommandAuditStore`) make Redis/DB adoption easier later
- **Cleaner ownership** for future team scaling

## Run locally (Java)
```bash
mvn -DskipTests package
mvn -pl command-service spring-boot:run
mvn -pl query-service spring-boot:run
```

Command service: `http://localhost:5477`
Query service: `http://localhost:5478`

## Run locally (Docker)
```bash
docker compose up --build
```

## JWT login
```bash
curl -X POST http://localhost:5477/blackrock/challenge/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```
Use the returned token in `Authorization: Bearer <token>` for both services.

## CQRS endpoint split
**Command service (5477)**
- `POST /blackrock/challenge/v1/transactions:parse`
- `POST /blackrock/challenge/v1/transactions:validator`
- `POST /blackrock/challenge/v1/transactions:filter`

**Query service (5478)**
- `POST /blackrock/challenge/v1/returns:nps`
- `POST /blackrock/challenge/v1/returns:index`
- `GET /blackrock/challenge/v1/performance`

## Next production step (recommended)
- Implement Redis adapters for `IdempotencyStore` + `ReadCacheStore`
- Implement PostgreSQL/Kafka adapter for `CommandAuditStore`
- Add API Gateway / Ingress auth offload if desired
- Add OpenTelemetry tracing + Prometheus metrics
