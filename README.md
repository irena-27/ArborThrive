# ArborThrive

**ArborThrive** is a post-hackathon evolution of a backend system built for transaction processing and returns computation.  
It follows a **CQRS-lite** architecture with a clear separation between:

- **Command Service** → parsing, validation, and filtering of transactions
- **Query Service** → returns calculation and runtime performance reporting

The goal of this branch is to evolve the original hackathon MVP into a **cleaner, more scalable, more maintainable, and more production-aware backend**.

---

## Project Status

> **Current branch:** `FB-ArborThrive`  
> This branch contains **post-hackathon engineering enhancements** on top of the original submission.

### Current implemented focus
- CQRS-lite command/query separation
- JWT-based authentication
- MDC-based correlation logging
- Savings pipeline / chain-based processing
- Resilience-oriented query orchestration
- Idempotency support for returns endpoints
- Structured logging and runtime metrics endpoint

### Important note
This README reflects the **current implemented architecture**, not the full future target state.  
At this stage, the project does **not** require:
- external database
- Redis
- service mesh
- API gateway

Those are possible future enhancements and are discussed in the roadmap.

---

## Why this project exists

The system is designed to solve a backend problem space around:

1. **Transaction parsing**
2. **Transaction validation**
3. **Temporal rule filtering**
4. **Returns calculation**
5. **Operational visibility and safe retries**

The architecture emphasizes:
- **clarity of business flow**
- **separation of responsibilities**
- **safe scaling path**
- **maintainable design patterns**
- **production-ready cross-cutting concerns**

---

## Architecture Overview

ArborThrive uses a **CQRS-lite** design:

### Command Service (`5477`)
Handles write-side / transformation-style operations:
- `POST /blackrock/challenge/v1/auth/login`
- `POST /blackrock/challenge/v1/transactions:parse`
- `POST /blackrock/challenge/v1/transactions:validator`
- `POST /blackrock/challenge/v1/transactions:filter`

### Query Service (`5478`)
Handles read-side / compute-heavy operations:
- `POST /blackrock/challenge/v1/returns:nps`
- `POST /blackrock/challenge/v1/returns:index`
- `GET /blackrock/challenge/v1/performance`

### Shared modules
- **shared-security**  
  JWT auth filter, security config, auth utilities

- **shared-domain / shared-core**  
  DTOs, business rules, pipeline chain, domain utilities

- **shared-observability**  
  MDC correlation logging, request timing, structured logging

---

## Architecture Diagram

<img width="975" height="510" alt="image" src="https://github.com/user-attachments/assets/56af86c4-789b-4af4-ad8e-0475ef7937e9" />


## Request Flow

### 1. Client request
The client sends a request with:
- `Authorization: Bearer <JWT>`
- `X-Correlation-Id`
- `Idempotency-Key` (recommended for returns)

### 2. Security filter chain
Every protected request goes through:
- `JwtAuthenticationFilter`
- `CorrelationIdFilter`
- exception mapping / CORS-lite

### 3. Controller + Facade orchestration
- **Command side** routes to `TransactionsController -> CommandFacade`
- **Query side** routes to `ReturnsController -> QueryFacade`

### 4. Domain execution
The shared `SavingsPipeline` applies business logic in a staged chain:
- parse
- validate
- K membership
- Q rules
- P rules
- final merge/filter

### 5. Query-side resilience
QueryFacade applies:
- rate limiting
- bulkhead isolation
- time limiting
- idempotent response reuse

### 6. Response + observability
The response is returned with:
- structured logs
- correlation id traceability
- runtime metrics available through `/performance`

---

## Design Patterns Used

### 1. CQRS-lite
Used to separate:
- command endpoints from
- query endpoints

**Why:**  
Improves maintainability, allows future independent scaling, and keeps responsibilities clearer.

---

### 2. Facade Pattern
Used in:
- `CommandFacade`
- `QueryFacade`

**Why:**  
Keeps controllers thin and centralizes orchestration logic.

---

### 3. Pipeline / Chain Pattern
Used in `SavingsPipeline`.

**Why:**  
Breaks business flow into composable processing stages:
- parse
- validate
- apply rules
- merge results

This makes logic easier to test, extend, and reason about.

---

### 4. Strategy + Factory
Used in `ReturnsEngine`.

**Why:**  
Allows clean selection between:
- NPS calculation flow
- Index calculation flow

This keeps returns computation extensible.

---

### 5. Filter-based cross-cutting design
Used for:
- JWT authentication
- correlation id propagation

**Why:**  
Ensures security and observability concerns are handled consistently before business execution.

---

## Key Features

- **JWT Authentication**
- **MDC Correlation Logging**
- **Command/Query separation**
- **Idempotency support**
- **Resilience-oriented query path**
- **Performance monitoring endpoint**
- **Chain-based rule processing**
- **Challenge-style raw transaction input support**

---
