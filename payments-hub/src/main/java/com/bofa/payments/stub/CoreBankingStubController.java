package com.bofa.payments.stub;

import com.bofa.common.money.Money;
import com.bofa.common.util.AccountNumbers;
import com.bofa.common.util.BankDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Local-profile stub of the core-banking account and transfer endpoints, so the hub can run without
 * the platform. Latency is configurable at runtime through {@code POST /__stub/core/latency} to
 * rehearse core-banking slowdowns (EOD batch, GC pauses).
 */
@RestController
@RequestMapping("/__stub/core")
@Profile("local")
@ApiIgnore
public class CoreBankingStubController {

    private static final Logger log = LoggerFactory.getLogger("core-banking-stub");

    private final StubLedger ledger;
    private volatile long baseLatencyMs = 40;
    private volatile double slowRatio = 0.0;
    private volatile long slowLatencyMs = 2600;

    public CoreBankingStubController(StubLedger ledger) {
        this.ledger = ledger;
    }

    @GetMapping("/api/v1/accounts/{accountNumber}")
    public ResponseEntity<?> account(@PathVariable String accountNumber) {
        StubLedger.StubAccount a = ledger.account(accountNumber);
        if (a == null) {
            return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "No such account");
        }
        return ResponseEntity.ok(view(a));
    }

    @GetMapping("/api/v1/customers/{cif}/accounts")
    public List<Map<String, Object>> customerAccounts(@PathVariable String cif) {
        return ledger.accountsFor(cif).stream().map(this::view).collect(Collectors.toList());
    }

    @PostMapping("/api/v1/transfers")
    public ResponseEntity<?> transfer(@RequestBody Map<String, String> body) throws InterruptedException {
        String from = body.get("fromAccount");
        String to = body.get("toAccount");
        String key = body.get("idempotencyKey");
        if (ledger.transferByKey(key) != null) {
            return error(HttpStatus.CONFLICT, "DUPLICATE", "Idempotency key already used");
        }
        StubLedger.StubAccount source = ledger.account(from);
        if (source == null || ledger.account(to) == null) {
            return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "No such account");
        }
        Money amount = Money.of(body.get("amount"));
        if (amount.isGreaterThan(source.available())) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, "NSF", "Insufficient available funds");
        }
        Date now = new Date();
        Map<String, String> posted = ledger.post(from, to, amount, body.get("memo"), key,
                BankDates.formatTimestamp(now), BankDates.formatDate(BankDates.businessDate(now)));
        log.info("posted {} {} -> {} amount={} key={}", posted.get("transferId"), AccountNumbers.mask(from),
                AccountNumbers.mask(to), amount.getAmount(), key);
        long delay = ThreadLocalRandom.current().nextDouble() < slowRatio ? slowLatencyMs : baseLatencyMs;
        Thread.sleep(delay);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("transferId", posted.get("transferId"));
        response.put("status", posted.get("status"));
        response.put("postedAt", posted.get("postedAt"));
        response.put("businessDate", posted.get("businessDate"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/transfers")
    public List<Map<String, String>> transfers() {
        return ledger.transfers();
    }

    @PostMapping("/latency")
    public Map<String, Object> latency(@RequestParam(required = false) Long baseMs,
                                       @RequestParam(required = false) Double slowRatio,
                                       @RequestParam(required = false) Long slowMs) {
        if (baseMs != null) {
            this.baseLatencyMs = baseMs;
        }
        if (slowRatio != null) {
            this.slowRatio = slowRatio;
        }
        if (slowMs != null) {
            this.slowLatencyMs = slowMs;
        }
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("baseMs", baseLatencyMs);
        state.put("slowRatio", this.slowRatio);
        state.put("slowMs", slowLatencyMs);
        return state;
    }

    private Map<String, Object> view(StubLedger.StubAccount a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("accountNumber", a.number);
        m.put("maskedNumber", AccountNumbers.mask(a.number));
        m.put("cif", a.cif);
        m.put("productCode", a.product);
        m.put("nickname", a.nickname);
        m.put("status", "OPEN");
        m.put("ledgerBalance", a.ledger.getAmount().toPlainString());
        m.put("availableBalance", a.available().getAmount().toPlainString());
        m.put("holdAmount", a.hold.getAmount().toPlainString());
        m.put("openedOn", a.openedOn);
        return m;
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
