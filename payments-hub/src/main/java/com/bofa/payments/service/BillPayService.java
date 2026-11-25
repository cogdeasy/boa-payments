package com.bofa.payments.service;

import com.bofa.common.money.Money;
import com.bofa.common.util.AccountNumbers;
import com.bofa.common.util.AuditLogger;
import com.bofa.common.util.BankDates;
import com.bofa.payments.core.CoreAccount;
import com.bofa.payments.core.CoreBankingClient;
import com.bofa.payments.core.CoreBankingException;
import com.bofa.payments.domain.Payment;
import com.bofa.payments.domain.PaymentStatus;
import com.bofa.payments.domain.PaymentType;
import com.bofa.payments.repository.PaymentRepository;
import com.bofa.payments.web.dto.BillPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class BillPayService {

    private static final Logger log = LoggerFactory.getLogger(BillPayService.class);

    static final Money MAX_BILL_PAYMENT = Money.of("25000.00");
    static final int MAX_DAYS_AHEAD = 365;

    private final PaymentRepository payments;
    private final PayeeService payeeService;
    private final CoreBankingClient core;

    public BillPayService(PaymentRepository payments, PayeeService payeeService, CoreBankingClient core) {
        this.payments = payments;
        this.payeeService = payeeService;
        this.core = core;
    }

    /**
     * Accepts a bill payment for a future (or today's) send-on date. Money does not move here; the
     * {@code BillPaymentReleaseJob} posts it on the send-on business date.
     */
    @Transactional
    public Payment schedule(BillPaymentRequest request, String actor) {
        Money amount = Money.of(request.getAmount());
        if (amount.compareTo(Money.zero()) <= 0 || amount.isGreaterThan(MAX_BILL_PAYMENT)) {
            throw new PaymentRejectedException("INVALID_AMOUNT", "Amount must be between 0.01 and 25000.00");
        }
        Date today = BankDates.businessDate(new Date());
        Date sendOn = BankDates.parseDate(request.getSendOn());
        if (sendOn.before(today)) {
            throw new PaymentRejectedException("INVALID_SEND_ON", "Send-on date is in the past");
        }
        if (sendOn.after(BankDates.addDays(today, MAX_DAYS_AHEAD))) {
            throw new PaymentRejectedException("INVALID_SEND_ON", "Send-on date is more than a year ahead");
        }
        if (!AccountNumbers.isValid(request.getFromAccount())) {
            throw new PaymentRejectedException("INVALID_ACCOUNT", "Invalid account number");
        }
        CoreAccount account = lookupAccount(request.getFromAccount());
        if (!request.getCif().equals(account.getCif()) || !account.isOpen()) {
            throw new PaymentRejectedException("INVALID_ACCOUNT", "Account cannot be used for bill pay");
        }
        payeeService.requireActivePayee(request.getCif(), request.getPayeeId());

        Payment payment = new Payment();
        payment.setType(PaymentType.BILL);
        payment.setCif(request.getCif());
        payment.setFromAccount(request.getFromAccount());
        payment.setPayeeId(request.getPayeeId());
        payment.setAmount(amount);
        payment.setSendOn(sendOn);
        payment.setStatus(PaymentStatus.SCHEDULED);
        payment.setCreatedAt(new Date());
        payment = payments.save(payment);
        payment.setPaymentId(PaymentReferences.paymentId("BP", today, payment.getId()));

        log.info("bill payment scheduled paymentId={} payeeId={} amount={} sendOn={}", payment.getPaymentId(),
                payment.getPayeeId(), amount.getAmount(), request.getSendOn());
        AuditLogger.record("AUD-BP-SCHED", actor, "paymentId=" + payment.getPaymentId() + " cif=" + payment.getCif());
        return payment;
    }

    private CoreAccount lookupAccount(String accountNumber) {
        try {
            return core.getAccount(accountNumber);
        } catch (CoreBankingException e) {
            throw new PaymentRejectedException(e.getCode(), "Account lookup failed");
        }
    }
}
