package com.bofa.payments.stub;

import com.bofa.common.money.Money;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory stand-in for the core-banking ledger, seeded with the platform's local-profile accounts.
 */
@Component
@Profile("local")
public class StubLedger {

    private final Map<String, StubAccount> accounts = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> transfersByKey = new ConcurrentHashMap<>();
    private final List<Map<String, String>> transfers = Collections.synchronizedList(new ArrayList<>());
    private int transferSeq = 230;

    public StubLedger() {
        add("104000123455", "100000001", "DDA-CHK", "Advantage Checking", "4812.33", "100.00", "2014-03-11");
        add("104000123463", "100000001", "DDA-SAV", "Advantage Savings", "22150.00", "0.00", "2014-03-11");
        add("211000048218", "100000002", "DDA-CHK", "Advantage Checking", "1045.12", "0.00", "2019-08-02");
        add("211000048226", "100000002", "DDA-SAV", "Advantage Savings", "9300.00", "0.00", "2019-08-02");
        add("307000555010", null, "GL-SUSP", "Payments suspense", "0.00", "0.00", "2009-01-01");
    }

    private void add(String number, String cif, String product, String nickname, String ledger, String hold,
                     String openedOn) {
        accounts.put(number, new StubAccount(number, cif, product, nickname, Money.of(ledger), Money.of(hold), openedOn));
    }

    public synchronized StubAccount account(String number) {
        return accounts.get(number);
    }

    public synchronized List<StubAccount> accountsFor(String cif) {
        List<StubAccount> result = new ArrayList<>();
        for (StubAccount a : accounts.values()) {
            if (cif.equals(a.cif)) {
                result.add(a);
            }
        }
        return result;
    }

    public Map<String, String> transferByKey(String key) {
        return key == null ? null : transfersByKey.get(key);
    }

    public synchronized Map<String, String> post(String from, String to, Money amount, String memo, String key,
                                                 String postedAt, String businessDate) {
        accounts.get(from).ledger = accounts.get(from).ledger.minus(amount);
        accounts.get(to).ledger = accounts.get(to).ledger.plus(amount);
        Map<String, String> t = new LinkedHashMap<>();
        t.put("transferId", "TRF-" + businessDate.replace("-", "") + "-" + String.format("%06d", ++transferSeq));
        t.put("status", "POSTED");
        t.put("postedAt", postedAt);
        t.put("businessDate", businessDate);
        t.put("fromAccount", from);
        t.put("toAccount", to);
        t.put("amount", amount.getAmount().toPlainString());
        t.put("memo", memo);
        t.put("idempotencyKey", key);
        transfers.add(t);
        if (key != null) {
            transfersByKey.put(key, t);
        }
        return t;
    }

    public List<Map<String, String>> transfers() {
        synchronized (transfers) {
            return new ArrayList<>(transfers);
        }
    }

    public static class StubAccount {
        final String number;
        final String cif;
        final String product;
        final String nickname;
        final String openedOn;
        Money ledger;
        Money hold;

        StubAccount(String number, String cif, String product, String nickname, Money ledger, Money hold,
                    String openedOn) {
            this.number = number;
            this.cif = cif;
            this.product = product;
            this.nickname = nickname;
            this.ledger = ledger;
            this.hold = hold;
            this.openedOn = openedOn;
        }

        Money available() {
            return ledger.minus(hold);
        }
    }
}
