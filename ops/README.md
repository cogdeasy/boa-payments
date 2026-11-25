# Operations

## Logs

`logs/payments-hub.log` is a production log extract from the Thursday 2026-11-18 morning session
(08:00-12:00 ET, pod `payments-hub-7d8c5`), kept for the open contact-centre investigation below.
`logs/p2p-error-rate-2026-11-18.csv` is the per-endpoint error rate and p95 the log pipeline derived
from the `http` completion lines for `POST /api/v1/payments/p2p`.

Log line shapes worth knowing:

| logger | meaning |
|---|---|
| `http` | one line per API call: `METHOD URI status=<code> durationMs=<n>` |
| `c.n.payments.core.CoreBankingClient` | one line per core-banking call out, with the idempotency key |
| `c.n.p.config.CoreBankingClientConfig` | retry warning, one per failed attempt |
| `AUDIT` | SOX audit trail (`AUD-*`), written through nb-common `AuditLogger` |

`rid` in the MDC is the edge request id (`X-Request-Id`), so all lines for one customer request can be
grepped together.

## Open items

### PMT-2291 — elevated 5xx on `POST /p2p`, duplicate P2P debits reported

Raised by the contact centre on 2026-11-18. Two symptoms during the 10:42-11:34 ET window:

1. Customers saw "Payment could not be completed, please try again" (HTTP 504) on Send Money.
2. 43 Send Money requests were debited more than once for the same payment (29 three times, 14
   twice). 24 of those customers also saw the error above and some retried from the app, making it
   worse. Statements show separate `TRF-*` references for each debit, all carrying the same `P2P-*`
   reference in the memo.

Core-banking platform engineering confirmed a GC pause / connection-pool saturation incident on
`core-banking.nbfb.internal` for that window and considers their side closed: every posting request
they received was well formed and was posted exactly once.

Payments is on the hook for the duplicate debits. Repro and root cause are still open; start from the
`rid` of one affected request in `logs/payments-hub.log` and follow it through the retry warnings.
Reconciliation has been handling the refunds manually against the suspense GL.
