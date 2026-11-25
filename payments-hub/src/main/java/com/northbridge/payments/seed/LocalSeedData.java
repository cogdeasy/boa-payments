package com.northbridge.payments.seed;

import com.northbridge.common.crypto.LegacyTokenCipher;
import com.northbridge.common.money.Money;
import com.northbridge.common.util.BankDates;
import com.northbridge.payments.domain.Payee;
import com.northbridge.payments.domain.Payment;
import com.northbridge.payments.domain.PaymentStatus;
import com.northbridge.payments.domain.PaymentType;
import com.northbridge.payments.repository.PayeeRepository;
import com.northbridge.payments.repository.PaymentRepository;
import com.northbridge.payments.service.PaymentReferences;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/** Seed payees and payment activity for the local profile (matches the platform seed customers). */
@Component
@Profile("local")
public class LocalSeedData implements CommandLineRunner {

    private final PayeeRepository payees;
    private final PaymentRepository payments;

    public LocalSeedData(PayeeRepository payees, PaymentRepository payments) {
        this.payees = payees;
        this.payments = payments;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (payees.count() > 0) {
            return;
        }
        payee("100000001", "Con Edison", "UTILITY", "4410227781");
        payee("100000001", "Verizon Wireless", "TELECOM", "882301945500");
        payee("100000001", "Northbridge Visa Signature", "CARD", "4532015112830366");
        payee("100000002", "National Grid", "UTILITY", "77120334");
        payee("100000002", "GEICO", "INSURANCE", "6029118842");

        Date today = BankDates.businessDate(new Date());
        bill("100000001", "104000123455", "PY-1001", "112.40", BankDates.addDays(today, -5), PaymentStatus.RELEASED);
        p2p("100000001", "104000123455", "jordan.lee@example.com", null, "45.00", "Dinner", BankDates.addDays(today, -3));
        bill("100000001", "104000123455", "PY-1002", "89.99", BankDates.addDays(today, 1), PaymentStatus.SCHEDULED);
        bill("100000002", "211000048218", "PY-1004", "76.20", BankDates.addDays(today, -8), PaymentStatus.RELEASED);
        p2p("100000002", "211000048218", null, "2125550147", "20.00", "Lunch", BankDates.addDays(today, -2));
        bill("100000002", "211000048218", "PY-1005", "131.50", today, PaymentStatus.SCHEDULED);
    }

    private void payee(String cif, String name, String category, String accountNumber) {
        Payee p = new Payee();
        p.setCif(cif);
        p.setName(name);
        p.setCategory(category);
        p.setAccountToken(LegacyTokenCipher.encrypt(accountNumber));
        p.setAccountLast4(accountNumber.substring(accountNumber.length() - 4));
        p = payees.save(p);
        p.setPayeeId(PaymentReferences.payeeId(p.getId()));
    }

    private void bill(String cif, String from, String payeeId, String amount, Date sendOn, PaymentStatus status) {
        Payment p = base(PaymentType.BILL, cif, from, amount, sendOn, status);
        p.setPayeeId(payeeId);
        finish(p, "BP");
    }

    private void p2p(String cif, String from, String email, String phone, String amount, String memo, Date sentOn) {
        Payment p = base(PaymentType.P2P, cif, from, amount, sentOn, PaymentStatus.SENT);
        p.setRecipientEmail(email);
        p.setRecipientPhone(phone);
        p.setMemo(memo);
        finish(p, "P2P");
    }

    private Payment base(PaymentType type, String cif, String from, String amount, Date date, PaymentStatus status) {
        Payment p = new Payment();
        p.setType(type);
        p.setCif(cif);
        p.setFromAccount(from);
        p.setAmount(Money.of(amount));
        p.setSendOn(date);
        p.setStatus(status);
        Date created = status == PaymentStatus.SCHEDULED ? BankDates.addDays(new Date(), -1) : date;
        p.setCreatedAt(created);
        return p;
    }

    private void finish(Payment p, String prefix) {
        p = payments.save(p);
        p.setPaymentId(PaymentReferences.paymentId(prefix, BankDates.businessDate(p.getCreatedAt()), p.getId()));
        if (p.getStatus() == PaymentStatus.RELEASED || p.getStatus() == PaymentStatus.SENT) {
            p.setCoreTransferId(String.format("TRF-%s-%06d",
                    BankDates.formatDate(p.getSendOn()).replace("-", ""), 100 + p.getId()));
            p.setReleasedAt(p.getSendOn());
        }
    }
}
