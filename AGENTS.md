# AGENTS.md

Notes for anyone (human or automated) working in this repository.

## Layout

* `pom.xml` reactor: `libs/nb-common` (pinned shared library, Java 8) then `payments-hub` (Java 11).
* `payments-hub/src/main/java/com/northbridge/payments` app code; packages `web`, `service`, `job`,
  `core`, `domain`, `repository`, `export`, `security`, `config`, `seed`, `stub`.
* `payments-hub/src/main/resources` `application*.yml` per profile (`local`, `platform`, `oracle`).
* `ops/` production log extracts and open operational items.

## Commands

JDK 11 must be on `JAVA_HOME`.

```bash
mvn clean install                         # full build + tests (also installs nb-common 4.11.3 locally)
mvn -pl payments-hub test                 # payments tests only (after one install)
mvn -pl payments-hub test -Dtest=BillPayServiceTest
mvn verify                                # tests + JaCoCo report in payments-hub/target/site/jacoco
java -jar payments-hub/target/payments-hub.jar                      # standalone, local profile
java -jar payments-hub/target/payments-hub.jar --spring.profiles.active=platform   # real core-banking on :8080
```

Smoke test once running:

```bash
curl -u dana.whitfield:Welcome1! 'http://localhost:8081/api/v1/payments/payees?cif=100000001'
```

Swagger UI: `http://localhost:8081/swagger-ui.html`.

Local stub controls (local profile only): `GET /__stub/core/api/v1/transfers` lists everything
posted to the stub ledger; `POST /__stub/core/latency?slowRatio=0.5` makes that share of transfer
calls slower than the client read timeout.

## Conventions

* API contract is owned by the Core Platform (`docs/API.md` there). Do not rename endpoints or
  fields here without a matching change upstream.
* Amounts: always `Money` (nb-common), never `double`. On the wire they are two-place decimal
  strings.
* Dates: `BankDates` / America/New_York. No `LocalDate.now()` without the bank zone.
* Account numbers: validate with `AccountNumbers.isValid`, log only `AccountNumbers.mask(...)`.
* Every state change on a payment writes an `AUD-*` line through `AuditLogger`.
* Calls to core-banking go through `CoreBankingClient` and the `coreRetry` template only.
* Customer-facing errors are `{code, message}` via `ApiExceptionHandler`; add a code there rather than
  returning ad-hoc bodies.
* Tests: JUnit 5 + Spring Boot test slices / MockMvc. Name them `<Class>Test`.
* Do not edit `libs/nb-common` in place; it mirrors an upstream release. Changes go upstream first
  and come back as a new pinned version (see `libs/README.md`).
* Commit messages: `PMT-<ticket>: <summary>`.
