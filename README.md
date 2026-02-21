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

## POST /transactions:parse

Converts raw expenses into enriched transactions by computing:
ceiling = next multiple of 100
remanent = ceiling - amount (micro-savings)
Auth: Required
Request body (array of expenses)
[
  {"date":"2023-10-12 20:15:30","amount":250},
  {"date":"2023-02-28 15:49:20","amount":375}
]

Success response (200) – array of enriched transactions
[
  {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50},
  {"date":"2023-02-28 15:49:20","amount":375,"ceiling":400,"remanent":25}
]

Errors
400 Bad Request – invalid date format / malformed JSON
401 Unauthorized – missing/invalid JWT

Curl

curl --location "http://localhost:5477/blackrock/challenge/v1/transactions:parse" \
  --header "Authorization: Bearer <JWT>" \
  --header "Content-Type: application/json" \
  --data '[{"date":"2023-10-12 20:15:30","amount":250},{"date":"2023-02-28 15:49:20","amount":375}]'
<img width="1710" height="1057" alt="Screenshot 2026-02-21 173845" src="https://github.com/user-attachments/assets/02038ed4-f439-4ec0-9db7-4ff4ba02107b" />


## POST /transactions:validator

Validates enriched transactions (typically output of transactions:parse) using rules like:
invalid/negative/zero amounts
duplicates (same date + amount)
maximum investment constraints (based on wage, if configured)
Returns split: valid[] and invalid[] (with reasons).
Auth: Required
Request body
{
  "wage": 50000,
  "transactions": [
    {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50},
    {"date":"2023-12-17 08:09:45","amount":-10,"ceiling":0,"remanent":0}
  ]
}
Success response (200)
{
  "valid": [
    {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50}
  ],
  "invalid": [
    {
      "date":"2023-12-17 08:09:45",
      "amount":-10,
      "ceiling":0,
      "remanent":0,
      "reason":"Amount must be > 0"
    }
  ]
}

Errors
400 Bad Request – missing fields / malformed JSON
401 Unauthorized – missing/invalid JWT

Curl

curl --location "http://localhost:5477/blackrock/challenge/v1/transactions:validator" \
  --header "Authorization: Bearer <JWT>" \
  --header "Content-Type: application/json" \
  --data '{
    "wage": 50000,
    "transactions": [
      {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50}
    ]
  }'
<img width="1781" height="1049" alt="Screenshot 2026-02-21 174054" src="https://github.com/user-attachments/assets/6607b19e-7071-4b1a-b5ea-a2d24e262bce" />


## POST /transactions:filter

Applies time-based rules to transactions:

q: fixed override for invest amount within range (overlaps resolved by “latest start wins”)

p: additive extra invest amount within range (sum of active p ranges)

k: membership windows used to classify whether transaction is eligible (if k provided)

Returns split: valid[] and invalid[].

Auth: Required
Request body
{
  "wage": 50000,
  "q": [
    {"fixed": 0, "start":"2023-07-01 00:00:00", "end":"2023-07-31 23:59:59"}
  ],
  "p": [
    {"extra": 30, "start":"2023-10-01 00:00:00", "end":"2023-12-31 23:59:59"}
  ],
  "k": [
    {"start":"2023-01-01 00:00:00", "end":"2023-12-31 23:59:59"}
  ],
  "transactions": [
    {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50}
  ]
}

Success response (200)
{
  "valid": [
    {"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":80}
  ],
  "invalid": []
}

Example above: original remanent=50, plus p-extra +30 → remanent=80 (q not active for that date).

Errors
400 Bad Request – invalid date ranges / malformed JSON
401 Unauthorized – missing/invalid JWT

Curl
curl --location "http://localhost:5477/blackrock/challenge/v1/transactions:filter" \
  --header "Authorization: Bearer <JWT>" \
  --header "Content-Type: application/json" \
  --data '{
    "wage": 50000,
    "q": [{"fixed": 0, "start":"2023-07-01 00:00:00", "end":"2023-07-31 23:59:59"}],
    "p": [{"extra": 30, "start":"2023-10-01 00:00:00", "end":"2023-12-31 23:59:59"}],
    "k": [{"start":"2023-01-01 00:00:00", "end":"2023-12-31 23:59:59"}],
    "transactions": [{"date":"2023-10-12 20:15:30","amount":250,"ceiling":300,"remanent":50}]
  }'

  <img width="1329" height="896" alt="Screenshot 2026-02-21 174136" src="https://github.com/user-attachments/assets/20662d0b-4d39-49e0-ba72-c7bdfe85f8c1" />

## POST /returns:nps
Computes savings grouped by k windows and simulates NPS returns (inflation-adjusted).
This endpoint can be called with raw transactions (date + amount); the service parses/validates internally.

Auth: Required
Recommended headers: Idempotency-Key + X-Correlation-Id

Request body
{
  "age": 29,
  "wage": 50000,
  "inflation": 5.5,
  "q": [{"fixed": 0, "start":"2023-07-01 00:00:00", "end":"2023-07-31 23:59:59"}],
  "p": [{"extra": 25, "start":"2023-10-01 08:00:00", "end":"2023-12-31 19:59:59"}],
  "k": [
    {"start":"2023-01-01 00:00:00", "end":"2023-12-31 23:59:59"},
    {"start":"2023-03-01 00:00:00", "end":"2023-11-30 23:59:59"}
  ],
  "transactions": [
    {"date":"2023-02-28 15:49:20","amount":375},
    {"date":"2023-07-01 21:59:00","amount":620},
    {"date":"2023-10-12 20:15:30","amount":250},
    {"date":"2023-12-17 08:09:45","amount":480}
  ]
}

Success response (200)
{
  "transactionsTotalAmount": 1725.0,
  "transactionsTotalCeiling": 1900.0,
  "savingsByDates": [
    {
      "start":"2023-01-01 00:00:00",
      "end":"2023-12-31 23:59:59",
      "amount":145.0,
      "profits":86.8848,
      "taxBenefit":0.0
    }
  ]
}

## Idempotency behavior
If you send the same Idempotency-Key again, the service returns the cached response for that key.

Errors
400 Bad Request – invalid JSON / invalid dates
401 Unauthorized – missing/invalid JWT
429 Too Many Requests – rate limiter/bulkhead triggered (Resilience4j)
504/500 – timeout or server error (depends on configuration)

Curl

curl --location "http://localhost:5477/blackrock/challenge/v1/returns:nps" \
  --header "Authorization: Bearer <JWT>" \
  --header "Content-Type: application/json" \
  --header "X-Correlation-Id: hackathon-demo-1" \
  --header "Idempotency-Key: nps-demo-key-1" \
  --data '{
    "age": 29,
    "wage": 50000,
    "inflation": 5.5,
    "q": [{"fixed": 0, "start":"2023-07-01 00:00:00", "end":"2023-07-31 23:59:59"}],
    "p": [{"extra": 25, "start":"2023-10-01 08:00:00", "end":"2023-12-31 19:59:59"}],
    "k": [{"start":"2023-01-01 00:00:00", "end":"2023-12-31 23:59:59"}],
    "transactions": [
      {"date":"2023-02-28 15:49:20","amount":375},
      {"date":"2023-07-01 21:59:00","amount":620},
      {"date":"2023-10-12 20:15:30","amount":250},
      {"date":"2023-12-17 08:09:45","amount":480}
    ]
  }'

  <img width="1353" height="877" alt="Screenshot 2026-02-21 174212" src="https://github.com/user-attachments/assets/7caba919-894b-4cd0-a9ee-a99bc329c828" />

## POST /returns:index

Same behavior as /returns:nps, but uses Index Fund return strategy (inflation-adjusted).
Auth: Required
Recommended headers: Idempotency-Key + X-Correlation-Id

Request body: same schema as returns:nps

Success response (200) – same shape as returns:nps
{
  "transactionsTotalAmount": 1725.0,
  "transactionsTotalCeiling": 1900.0,
  "savingsByDates": [
    {
      "start":"2023-01-01 00:00:00",
      "end":"2023-12-31 23:59:59",
      "amount":145.0,
      "profits":91.1234,
      "taxBenefit":0.0
    }
  ]
}

Errors
400 Bad Request
401 Unauthorized
429 Too Many Requests
500/504 timeout/server error

Curl

curl --location "http://localhost:5477/blackrock/challenge/v1/returns:index" \
  --header "Authorization: Bearer <JWT>" \
  --header "Content-Type: application/json" \
  --header "X-Correlation-Id: hackathon-demo-1" \
  --header "Idempotency-Key: index-demo-key-1" \
  --data '{
    "age": 29,
    "wage": 50000,
    "inflation": 5.5,
    "q": [],
    "p": [],
    "k": [{"start":"2023-01-01 00:00:00", "end":"2023-12-31 23:59:59"}],
    "transactions": [{"date":"2023-10-12 20:15:30","amount":250}]
  }'

<img width="1332" height="890" alt="Screenshot 2026-02-21 174259" src="https://github.com/user-attachments/assets/f1ef124c-7bb1-43b2-89eb-bb9f9bde953e" />

## GET /performance

Returns simple runtime statistics for quick debugging & demo:
last request latency
memory usage snapshot
current thread count

Auth: Required

Success response (200)
{
  "time": 12,
  "memory": 123456789,
  "threads": 42
}

Errors
401 Unauthorized – missing/invalid JWT

Curl

curl --location "http://localhost:5477/blackrock/challenge/v1/performance" \
  --header "Authorization: Bearer <JWT>"
<img width="1367" height="562" alt="Screenshot 2026-02-21 174315" src="https://github.com/user-attachments/assets/33b8dc5a-49c5-4c54-a7bd-cf84d6c1d09c" />

  
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

## Edge cases & bottlenecks handled
- Negative/zero amount → invalid
- Duplicate transactions → HashSet detection (date+amount key)
- Overlapping q → deterministic selection (latest start wins)
- Overlapping p → additive via running sum
- Overlapping k → merge intervals for membership checks
- Inclusive boundaries handled consistently (start <= t <= end)
- JWT in async responses handled (filters enabled for async dispatch)
- Idempotency prevents duplicate results on retries

## Bottlenecks
Compute-heavy returns endpoints
bulkhead + dedicated executor + timeout
Traffic spikes/abuse
rate limiter
Retry storms
idempotency
Debugging complexity
correlation-id trace in logs

## Risks & future improvements
Current limitations
In-memory idempotency store (lost on restart; not shared across replicas)
No persistent portfolio store
JWT secret in config can be vaulted in real deployment
Strict date format assumptions (timezone upgrades needed for production)

-Future improvements
Redis-backed idempotency with TTL
Cache returns by normalized request hash
Prometheus + Grafana metrics
Split into two deployables (Command Service / Query Service) for scaling
Refresh tokens + RBAC + per-user rate limiting
