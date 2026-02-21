** Objective of the problem statement & what our project does**
Problem objective

 A backend service that helps an investor automatically save and invest small amounts (“round-up savings”) from day-to-day spending, and then simulate long-term returns across instruments (NPS / Index fund) while applying time-based investment rules.

This provides REST APIs (port 5477) that:

Parses expenses and converts them into micro-savings:

For each expense amount, compute:

ceiling: next multiple of 100

remanent: ceiling - amount (the investable “round-up”)

Validates transactions based on constraints (salary/wage cap, duplicates, negative amounts, etc.) and returns:

valid[] and invalid[] with reasons

Applies time rules (q / p / k):

q: override invest amount with a fixed value during a date range (with “latest-start wins” rule)

p: add extra invest amount during a date range (all matching ranges add up)

k: defines one or more “investment windows” used to group and compute savings/returns

Computes returns:

returns:nps and returns:index calculate profits per k window, with inflation adjustment

NPS endpoint can include tax-benefit logic (if configured)

Includes production-grade concerns:

JWT authentication

MDC correlation ID for traceable logs

Idempotency key to prevent duplicated processing on retries

Resilience4j (rate limiting/bulkhead/timeout) to prevent overload

Flow of the project
Request lifecycle

<img width="4212" height="2392" alt="architecture_diagram_uploaded_style_clean" src="https://github.com/user-attachments/assets/f058766b-c021-4529-bcb2-df5320d0e74e" />

Ingress

Request hits Spring Boot (port 5477)

CorrelationIdFilter (MDC)

Reads X-Correlation-Id

If missing, generates one

Puts it into MDC so every log line prints the same id

JWT Authentication Filter

Reads Authorization: Bearer <token>

Validates token

Sets SecurityContext with authenticated user

Controller

Routes to command or query side (CQRS-lite)

Command side (parse/validate/filter)

Uses a Pipeline (Chain) to execute steps in a stable, testable sequence

Query side (returns)

Uses:

Resilience guards (RateLimiter/Bulkhead/Timeout)

Idempotency (same key → same response)

Returns Strategy (NPS vs Index) via factory

Response

JSON returned to client

Logs contain correlation id for full traceability

HLD & LLD + design patterns 
HLD (High-Level Design)

Three clear areas (as in your diagram):

Ingress & Security

CorrelationIdFilter (MDC)

JwtAuthFilter

Spring Security config (stateless)

CQRS-lite + Core Domain

Commands: parse / validator / filter

Queries: returns / performance

Pipeline for domain processing

Strategies for return computation

Idempotency + Resilience for risk & scaling

Observability & Runtime

Request timing interceptor

/performance endpoint

Structured logging with correlation ID

LLD (Low-Level Design)

Typical packages/modules (conceptually):

api/ → controllers, filters, exception handler

cqrs/command → CommandFacade

cqrs/query → QueryFacade

pipeline/ → step interfaces + step implementations

returns/ → strategy + factory

spec/ → validation rules

idempotency/ → store + lookup

security/ → jwt service/filter/config

util/ → money math, date parsing, tax utils

Design patterns used (and why)
1) CQRS-lite

Why: command endpoints and query endpoints have different scaling/compute profiles
Advantage:

Easy to scale “returns” compute separately (dedicated executor/bulkhead)

Cleaner separation for future microservices split

2) Pipeline / Chain of Responsibility

Why: q/p/k logic is complex and grows easily
Advantage:

Each step is independent and unit-testable

Easier optimization per step

Lower regression risk (change one step, not the whole method)

3) Strategy + Factory

Why: returns logic differs between instruments but shares a common contract
Advantage:

Add new instruments without modifying controllers

Keeps logic open for extension, closed for modification (OCP)

4) Specification Pattern (Validation rules)

Why: validation grows into messy if/else blocks
Advantage:

Rules are composable and readable

Easy to add/remove rules

Better error reporting (invalid reason)

5) Idempotency key pattern

Why: retries can cause double-processing in real finance workflows
Advantage:

Same Idempotency-Key returns the same response

Protects from duplicate calls due to network retries

6) Resilience patterns

RateLimiter: prevent abuse / sudden spikes

Bulkhead: prevent returns computations from starving other endpoints

TimeLimiter: avoid requests hanging indefinitely

7) Interceptor + MDC logging

Why: debugging distributed systems needs correlation
Advantage:

Every log line ties back to a request

/performance gives quick runtime visibility

4) Algorithm optimizations (what we improved + why)
A) Parsing/Preparation optimizations

Parse date strings once → convert to timestamps

Sort transactions once → all sweep algorithms become linear
Benefit: avoids repeated parsing in loops (big CPU saver)

B) Q rules (override) optimization

Problem: naive approach checks every q range for every transaction → O(n*q)
Optimized algorithm: sweep line + max-heap

Sort q by start

While scanning transactions in time order:

add active q ranges into heap

remove expired

top of heap gives “latest start wins”
Complexity: O((n+q) log q)

C) P rules (add extras) optimization

Problem: naive O(n*p)
Optimized algorithm: event sweep

Convert each p range into two events:

start: +extra

end: -extra

Sweep through sorted events while scanning transactions
Complexity: O(p log p + n)

D) K membership (filter endpoint)

Problem: check tx against all k ranges is expensive
Optimized algorithm: merge intervals + single scan

Merge overlapping k ranges first

Then scan transactions with pointers
Complexity: O(k log k + n)

E) K aggregation (returns endpoints)

Goal: compute invested amount per k window fast
Optimized algorithm: prefix sums + binary search

Build prefix sum over remanents: prefix[i]

For each k:

find indices with lowerBound(start) and upperBound(end)

sum in O(1)
Complexity: O(n + k log n)

5) Edge cases + bottlenecks handled (and how)
Edge cases

Negative / zero amounts → invalid

Duplicate transactions → detected via HashSet (date+amount)

Overlapping q ranges → “latest start wins” enforced by heap ordering

Overlapping p ranges → additive handled by running sum

Overlapping k ranges → merged for membership checks

No k provided → treat as whole period or return empty savings windows (depending on spec choice)

Boundary inclusivity (start <= t <= end) handled consistently in event and range logic

JWT on async response fixed (async dispatch filters enabled)

Idempotency prevents repeat calls producing inconsistent responses

Bottlenecks & how we tackle them

Compute-heavy returns

Bulkhead + dedicated executor + time limiter

Spike traffic / abuse

Rate limiter (Resilience4j)

Debugging & traceability

MDC correlation + structured logging

Retry storms / duplicates

Idempotency-Key cache

Large q/p/k input sizes

Sweeps + prefix sums instead of nested loops

6) Risks & future improvements
Current risks / limitations

In-memory idempotency cache

loses state on restart

not shared across instances
✅ Fine for hackathon, but not production.

No persistent store

currently purely compute-based

no user portfolio persistence

JWT secret management

stored in config for demo

needs secure secret vault in real deployment

Date handling

relies on consistent input format

timezones can be tricky if extended

Future improvements (strong “scaling story”)

Externalize idempotency store

Redis with TTL for idempotency keys

distributed-safe across replicas

Caching

cache returns results by normalized request hash (not only idempotency key)

useful for repeated simulations

Observability

Prometheus metrics + Grafana dashboards

structured JSON logs + ELK stack integration

distributed tracing (OpenTelemetry)

Split into two services

Command service (parse/validate/filter)

Query service (returns/performance)

Scale query service independently (CQRS payoff)

More instruments

add strategies: PPF, FD, Gold, SIP, etc.

Better security

refresh tokens

role-based access (RBAC)

rate-limit per user (from JWT subject)
