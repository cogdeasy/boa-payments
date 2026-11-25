package com.bofa.payments.service;

import com.bofa.common.money.Money;
import com.bofa.common.util.AccountNumbers;
import com.bofa.common.util.AuditLogger;
import com.bofa.common.util.BankDates;
import com.bofa.payments.config.CoreBankingProperties;
import com.bofa.payments.core.CoreAccount;
import com.bofa.payments.core.CoreBankingClient;
import com.bofa.payments.core.CoreBankingException;
import com.bofa.payments.core.TransferRequest;
import com.bofa.payments.core.TransferResponse;
import com.bofa.payments.domain.Payment;
import com.bofa.payments.domain.PaymentStatus;
import com.bofa.payments.domain.PaymentType;
import com.bofa.payments.repository.PaymentRepository;
import com.bofa.payments.web.dto.P2pRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.Date;
import java.util.UUID;

/**
 * Person-to-person transfers to an email or US mobile number. Funds are debited from the customer's
 * DDA into the P2P settlement suspense GL immediately; the network settles from there.
 */
@Service
public class P2pService {

    private static final Logger log = LoggerFactory.getLogger(P2pService.class);

    static final Money PER_TRANSFER_LIMIT = Money.of("2500.00");
    static final Money DAILY_LIMIT = Money.of("2500.00");

    private final PaymentRepository payments;
    private final CoreBankingClient core;
    private final RetryTemplate coreRetry;
    private final CoreBankingProperties coreProps;

    public P2pService(PaymentRepository payments, CoreBankingClient core, RetryTemplate coreBankingRetryTemplate,
                      CoreBankingProperties coreProps) {
        this.payments = payments;
        this.core = core;
        this.coreRetry = coreBankingRetryTemplate;
        this.coreProps = coreProps;
    }

    @Transactional(noRollbackFor = {CoreUnavailableException.class, PaymentRejectedException.class})
    public Payment send(P2pRequest request, String actor) {
        P2pRequest.Recipient recipient = request.getRecipient();
        boolean hasEmail = recipient.getEmail() != null && !recipient.getEmail().isEmpty();
        boolean hasPhone = recipient.getPhone() != null && !recipient.getPhone().isEmpty();
        if (hasEmail == hasPhone) {
            throw new PaymentRejectedException("INVALID_RECIPIENT", "Provide exactly one of email or phone");
        }
        String recipientValue = hasEmail ? recipient.getEmail().trim().toLowerCase() : recipient.getPhone();

        log.info("P2P send requested cif={} fromAccount={} recipient={} amount={}", request.getCif(),
                request.getFromAccount(), recipientValue, request.getAmount());

        Money amount = Money.of(request.getAmount());
        if (amount.compareTo(Money.zero()) <= 0) {
            throw new PaymentRejectedException("INVALID_AMOUNT", "Amount must be positive");
        }
        if (amount.isGreaterThan(PER_TRANSFER_LIMIT)) {
            throw new PaymentRejectedException("LIMIT_EXCEEDED", "Amount exceeds the per-transfer P2P limit");
        }
        Date now = new Date();
        Date businessDate = BankDates.businessDate(now);
        Money sentToday = payments.sumSentSince(request.getCif(), PaymentType.P2P, businessDate);
        if (sentToday == null) {
            sentToday = Money.zero();
        }
        if (sentToday.plus(amount).isGreaterThan(DAILY_LIMIT)) {
            throw new PaymentRejectedException("LIMIT_EXCEEDED", "Amount exceeds the daily P2P limit");
        }
        if (!AccountNumbers.isValid(request.getFromAccount())) {
            throw new PaymentRejectedException("INVALID_ACCOUNT", "Invalid account number");
        }
        CoreAccount account;
        try {
            account = core.getAccount(request.getFromAccount());
        } catch (CoreBankingException e) {
            throw new PaymentRejectedException(e.getCode(), "Account lookup failed");
        }
        if (!account.isOpen()) {
            throw new PaymentRejectedException("ACCOUNT_NOT_OPEN", "Account is not open");
        }

        Payment payment = new Payment();
        payment.setType(PaymentType.P2P);
        payment.setCif(request.getCif());
        payment.setFromAccount(request.getFromAccount());
        if (hasEmail) {
            payment.setRecipientEmail(recipientValue);
        } else {
            payment.setRecipientPhone(recipientValue);
        }
        payment.setAmount(amount);
        payment.setMemo(request.getMemo());
        payment.setSendOn(businessDate);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setCreatedAt(now);
        payment = payments.save(payment);
        payment.setPaymentId(PaymentReferences.paymentId("P2P", businessDate, payment.getId()));
        final Payment p = payment;

        try {
            TransferResponse posted = coreRetry.execute(ctx -> core.postTransfer(new TransferRequest(
                    p.getFromAccount(),
                    coreProps.getSuspenseAccount(),
                    p.getAmount().getAmount().toPlainString(),
                    "P2P " + p.getPaymentId() + (p.getMemo() == null ? "" : " " + p.getMemo()),
                    UUID.randomUUID().toString())));
            payment.setStatus(PaymentStatus.SENT);
            payment.setCoreTransferId(posted.getTransferId());
            payment.setReleasedAt(new Date());
        } catch (CoreBankingException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getCode());
            log.warn("P2P rejected by core paymentId={} code={}", payment.getPaymentId(), e.getCode());
            throw new PaymentRejectedException(e.getCode(), "Transfer rejected");
        } catch (ResourceAccessException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("CORE_TIMEOUT");
            log.error("P2P failed paymentId={} core-banking unavailable after {} attempts", payment.getPaymentId(),
                    coreProps.getMaxAttempts());
            throw new CoreUnavailableException("core-banking unavailable", e);
        }

        log.info("P2P sent paymentId={} transferId={} amount={}", payment.getPaymentId(),
                payment.getCoreTransferId(), amount.getAmount());
        AuditLogger.record("AUD-P2P-SEND", actor, "paymentId=" + payment.getPaymentId() + " cif=" + payment.getCif());
        return payment;
    }
}
