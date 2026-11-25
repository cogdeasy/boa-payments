# payments-hub

Northbridge Federal Bank payments hub: bill pay, person-to-person (P2P) transfers and payee
management for retail customers. Owned by Payments Platform (`payments-platform@nbfb.internal`).

The service sits beside the Northbridge Core Platform. It never holds balances itself: every money
movement is posted to core-banking as a transfer into the payments suspense GL (`307000555010`),
and settlement out of suspense to the biller / P2P network is handled downstream.

| | |
|---|---|
| Port | `8081` |
| Base path | `/api/v1/payments` |
| Runtime | Java 11, Spring Boot 2.3.12, Spring Data JPA, Spring Security (HTTP Basic) |
| Database | H2 in-memory (default) / Oracle 19c (`oracle` profile) |
| API docs | Swagger 2 (springfox) at `/swagger-ui.html`, spec at `/v2/api-docs` |
| Shared library | `com.northbridge:nb-common:4.11.3` (vendored under `libs/nb-common`) |

## Quick start

Requirements: JDK 11 and Maven 3.6+.

```bash
mvn clean install                       # builds nb-common, then payments-hub, runs tests
java -jar payments-hub/target/payments-hub.jar
```

The default `local` profile runs fully standalone: it starts an in-process core-banking stub at
`/__stub/core/api/v1`, seeds payees and recent history for the two platform test customers, and
releases due bill payments every 30 seconds.

```bash
# payees for Dana Whitfield
curl -u dana.whitfield:Welcome1! 'http://localhost:8081/api/v1/payments/payees?cif=100000001'

# schedule a bill payment
curl -u dana.whitfield:Welcome1! -H 'Content-Type: application/json' \
  -d '{"cif":"100000001","fromAccount":"104000123455","payeeId":"PY-1001","amount":"84.20","sendOn":"2026-12-01"}' \
  http://localhost:8081/api/v1/payments/bill

# send money
curl -u dana.whitfield:Welcome1! -H 'Content-Type: application/json' \
  -d '{"cif":"100000001","fromAccount":"104000123455","recipient":{"email":"marcus.o@example.com"},"amount":"25.00","memo":"Lunch"}' \
  http://localhost:8081/api/v1/payments/p2p

# history, and the XML remittance export
curl -u dana.whitfield:Welcome1! 'http://localhost:8081/api/v1/payments/history?cif=100000001'
curl -u dana.whitfield:Welcome1! 'http://localhost:8081/api/v1/payments/history/export?cif=100000001'
```

Test users (local profile only) match the platform seed set:

| username | password | role | CIF |
|---|---|---|---|
| dana.whitfield | Welcome1! | CUSTOMER | 100000001 |
| marcus.oyelaran | Welcome1! | CUSTOMER | 100000002 |
| teller01 | Teller#2019 | TELLER | |
| ops.batch | Ops#Batch1 | OPS | |

Seed accounts: `104000123455` / `104000123463` (Dana, checking / savings), `211000048218` /
`211000048226` (Marcus, checking / savings), `307000555010` (payments suspense GL).

## Profiles

| profile | what it does |
|---|---|
| `local` (default) | in-process core-banking stub, local users, seed payees/history, 30s release job |
| `platform` | talks to a real core-banking on `http://localhost:8080/api/v1` (run the Core Platform locally) |
| `oracle` | Oracle datasource (`PAYHUB` schema), `ddl-auto: validate`; combine with `platform` in SIT/prod |

```bash
java -jar payments-hub/target/payments-hub.jar --spring.profiles.active=platform
java -jar payments-hub/target/payments-hub.jar --spring.profiles.active=platform,oracle
```

## API

| method | path | role | notes |
|---|---|---|---|
| GET | `/api/v1/payments/payees?cif=` | CUSTOMER, TELLER | active payees, account shown as last four |
| POST | `/api/v1/payments/payees` | CUSTOMER, TELLER | add payee |
| DELETE | `/api/v1/payments/payees/{payeeId}` | CUSTOMER, TELLER | soft delete |
| POST | `/api/v1/payments/bill` | CUSTOMER, TELLER | `{cif, fromAccount, payeeId, amount, sendOn}` -> `{paymentId, status: SCHEDULED}` |
| POST | `/api/v1/payments/p2p` | CUSTOMER, TELLER | `{cif, fromAccount, recipient: {email\|phone}, amount, memo}` -> `{paymentId, status: SENT}` |
| GET | `/api/v1/payments/history?cif=` | authenticated | optional `status`, `sort` |
| GET | `/api/v1/payments/history/export?cif=` | authenticated | XML remittance advice (nb-common `StatementXml`) |
| POST | `/api/v1/payments/ops/release-bills` | OPS | run the release job now |

Amounts are decimal strings with two places, dates are `yyyy-MM-dd` (bank time zone
America/New_York), account numbers are 12 digits and are masked to the last four in logs.
Errors use `{ "code": "...", "message": "..." }`, e.g. `NSF`, `INVALID_ACCOUNT`, `PAYEE_NOT_FOUND`,
`LIMIT_EXCEEDED`, `CORE_TIMEOUT`.

## Architecture

```
            +---------------------------- payments-hub :8081 -----------------------------+
 client --> | PaymentsController --> PayeeService / BillPayService / P2pService            |
            |        |                       |                     |                       |
            |  PaymentHistoryDao (JDBC)   JPA repositories     CoreBankingClient (RestTemplate
            |        \______________________/                   + RetryTemplate)           |
            |                  H2 / Oracle                            |                    |
            |  BillPaymentReleaseJob (@Scheduled) ---------------------+                    |
            +------------------------------------------------------------|-------------------+
                                                                         v
                                              core-banking :8080  GET /api/v1/accounts/{n}
                                                                  POST /api/v1/transfers
```

* `web` controllers, DTOs and the API error mapper.
* `service` business rules: limits, ownership checks, bill scheduling, P2P send.
* `job.BillPaymentReleaseJob` picks up `SCHEDULED` bill payments whose `sendOn` is today or
  earlier, checks available balance on core-banking and posts the transfer to suspense.
* `core` the core-banking client and its wire types.
* `export` XML remittance advice, marshalled with nb-common `StatementXml` (JAXB).
* `stub` the in-process core-banking stub used by the `local` profile.

### How payments talks to core-banking

* `GET /api/v1/accounts/{accountNumber}` to confirm the source account exists, is `OPEN`, belongs
  to the customer's CIF and has the available balance.
* `POST /api/v1/transfers` with `{fromAccount, toAccount: 307000555010, amount, memo, idempotencyKey}`.
  Core-banking deduplicates on `idempotencyKey` (24h window) and returns `409 DUPLICATE` with the
  original `transferId` on replay.
* Authenticated as the `ops.batch` service user over HTTP Basic. Connect timeout 1s, read timeout
  2s, up to 3 attempts with 250ms backoff on I/O errors (`core.banking.*` in `application.yml`).
* Bill payments use the `paymentId` as the idempotency key.

### nb-common

`libs/nb-common` is a pinned copy of `com.northbridge:nb-common:4.11.3` from the Core Platform
repository, so this service builds without access to the platform build. payments-hub uses it
directly:

| class | used for |
|---|---|
| `money.Money` | every amount in the domain, persisted through `MoneyConverter` |
| `util.AccountNumbers` | format/check-digit validation and masking |
| `util.BankDates` | bank time zone, business-date and `sendOn` parsing |
| `util.AuditLogger` | `AUD-*` audit trail |
| `xml.StatementXml` | XML remittance export (`javax.xml.bind`) |
| `crypto.LegacyTokenCipher` | tokenising payee account numbers at rest |

nb-common is compiled for Java 8 and is built on `javax.xml.bind` and `javax.*` APIs, so the JAXB
API and runtime are declared explicitly in `payments-hub/pom.xml` for Java 11. When nb-common moves
to Java 17 / `jakarta.*` this service has to move with it. See `libs/README.md` for how to refresh
the vendored copy.

## Operations

* Health: `/actuator/health`; metrics `/actuator/metrics`.
* Logs carry the request id from `X-Request-Id` (generated if absent) in the `rid` slot.
* Production log extracts and open investigations live in `ops/`.

## Build

* CI: `Jenkinsfile` (shared `maven && jdk11` agents, publishes to Nexus from `main`).
* Coverage: `mvn verify` writes JaCoCo to `payments-hub/target/site/jacoco`.
