# Ledger Engine

Double-entry ledger engine in Spring Boot with balance validation & immutable audit trail.

A backend service that models real double-entry bookkeeping: every transaction is split into balanced debit/credit entries, balances are always derived from entry history (never stored as mutable state), and nothing is ever deleted, only appended.

## Core Concept

- Every `Transaction` creates two or more `Entry` records (debits and credits) across accounts.
- The system enforces `sum(debits) == sum(credits)` for every transaction, or it's rejected.
- Balances are **computed on the fly** from entry history, never stored as a mutable field.
- All writes are append-only. Corrections happen via **reversal**, not deletion or update.

## Tech Stack

- **Java 17**
- **Spring Boot 4.1.0**
- **Spring Data JPA**: persistence layer
- **PostgreSQL**: primary datastore
- **Spring Security**: authentication/authorization
- **Lombok**: boilerplate reduction
- **Spring Validation**: request-level input validation
- **Spring Actuator**: health/metrics endpoints
- **Maven**: build tool

## Architecture

The codebase follows a domain-driven design (DDD) structure, organized by bounded context rather than technical layer:

```
com.king.ledgerengine
├── domain
│   ├── account       # Account entity, repository, service, controller
│   ├── entry         # Entry entity, repository (balance computation)
│   ├── transaction    # Transaction entity, repository, service, controller
│   └── user          # User entity, repository, service, controller
├── config            # Spring framework configuration (security, beans)
├── shared            # Cross-cutting concerns (exceptions, common DTOs)
└── LedgerEngineApplication
```

### Domain Model

| Entity | Description |
|---|---|
| `Account` | A ledger bucket. Has a name, type (`ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE`), and currency. Owned by a `User`. |
| `Transaction` | A business event. Has a description and status (`PENDING`, `POSTED`, `REVERSED`). Groups one or more `Entry` records. |
| `Entry` | A single debit or credit line against an account, tied to a transaction. Immutable once created. |
| `User` | The identity that owns accounts and authenticates against the API. |

### Why no `Balance` entity?

Balance is intentionally **not** a stored, mutable field. It's a derived value:

```
balance(account) = SUM(credits) - SUM(debits)
```

Storing it separately would risk drift between the "official" entry history and a cached number, exactly the class of bug double-entry accounting is designed to prevent. Balance is computed via an indexed `SUM()` query over `entries.account_id` at read time.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Register a new user |
| `GET` | `/users/{id}` | Get user by ID |
| `POST` | `/accounts` | Create a new account |
| `GET` | `/accounts/{id}/balance` | Get current computed balance |
| `GET` | `/accounts/{id}/entries` | Get full entry history for an account |
| `POST` | `/transactions` | Create a transaction with balanced entries (requires `Idempotency-Key` header) |
| `POST` | `/transactions/{id}/reverse` | Reverse a posted transaction via offsetting entries |

## Correctness & Concurrency Guarantees

- **Balanced-transaction validation**: a transaction is rejected at the service layer unless `sum(debits) == sum(credits)`.
- **Idempotency keys**: every `POST /transactions` request requires an `Idempotency-Key` header; retried requests with the same key return the original result instead of double-processing.
- **Atomicity**: transaction + entry creation is wrapped in a single `@Transactional` boundary; if any part fails, nothing commits.
- **Append-only entries**: entries have no `UPDATE`/`DELETE` path. Corrections are made via `POST /transactions/{id}/reverse`, which creates new offsetting entries rather than touching the original transaction.
- **Database-level constraints**: foreign keys, `NOT NULL`, and unique constraints (e.g. on `idempotency_key`) act as a second line of defense beyond application-level checks.

## Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `./mvnw` wrapper)
- PostgreSQL running locally

### Configuration

Set your database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ledger_engine
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Run the app

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Example: creating a transaction

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -H "X-User-Id: <user-id>" \
  -d '{
    "description": "Customer deposit",
    "entries": [
      { "accountId": "<cash-account-id>", "amount": 100.00, "type": "DEBIT" },
      { "accountId": "<liability-account-id>", "amount": 100.00, "type": "CREDIT" }
    ]
  }'
```

## Roadmap

- [x] Core domain model (Account, Transaction, Entry)
- [x] Balanced-transaction validation
- [x] Balance computation via aggregate query
- [x] Idempotency key enforcement
- [x] Transaction reversal (offsetting entries)
- [ ] Optimistic locking on Account
- [ ] Audit log table
- [ ] System-wide reconciliation endpoint
- [ ] Multi-currency support with exchange rate snapshots
- [ ] Scheduled/recurring transactions
- [ ] Event publishing on transaction posting
- [ ] API rate limiting

## Notes on Design Decisions

- **Amounts are stored as `BigDecimal`**, not floating-point, to avoid rounding errors inherent to `double`/`float` in financial calculations.
- **Entity relationships use `@ManyToOne`** from `Entry` to both `Transaction` and `Account`: many entries belong to one transaction, many entries belong to one account.
- **Package-by-domain, not package-by-layer**: each bounded context (`account`, `transaction`, `entry`, `user`) owns its own entity, repository, service, and controller, rather than grouping all controllers together, all services together, etc.