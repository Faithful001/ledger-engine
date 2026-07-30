Double-Entry Ledger Engine — Project Plan
Core Concept

Every transaction creates two entries (a debit and a credit) across accounts. The system enforces that debits always equal credits, balances are computed from entry history (not mutable fields), and nothing is ever deleted — only appended (immutable audit trail).

Phase 1: Core Domain
Account — id, name, type (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE), currency
Transaction — id, timestamp, description, status (PENDING, POSTED, REVERSED)
Entry — id, transaction_id, account_id, amount, type (DEBIT/CREDIT)
Balance — computed on-the-fly or maintained as a running snapshot per account

Business rule to enforce: sum(debits) == sum(credits) for every transaction, or it's rejected.

Phase 2: API Layer
POST /accounts — create account
POST /transactions — create a transaction with a list of entries (validated atomically)
GET /accounts/{id}/balance — current balance
GET /accounts/{id}/entries — full entry history (paginated)
POST /transactions/{id}/reverse — reverse a posted transaction (creates offsetting entries, never deletes)
Phase 3: Correctness & Concurrency
Idempotency keys on POST /transactions (prevent duplicate processing on retry)
Database-level constraints (CHECK constraints, foreign keys) as a second line of defense beyond application logic
Optimistic locking on accounts to handle concurrent balance updates safely
Wrap transaction creation in a single DB transaction — all entries commit or none do
Phase 4: Auditability
Every write is append-only (no UPDATE/DELETE on entries — corrections happen via reversal + new transaction)
Audit log table capturing who/when/what for every state change
Reconciliation job/endpoint that verifies system-wide debits == credits at any point in time
Phase 5 (stretch): Extras
Multi-currency support with exchange rate snapshots per transaction
Scheduled/recurring transactions
Event publishing (transaction posted → emit event) for downstream consumers
Basic rate limiting on the API