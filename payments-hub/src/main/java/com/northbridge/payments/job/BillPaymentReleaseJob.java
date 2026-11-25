package com.northbridge.payments.job;

import com.northbridge.common.money.Money;
import com.northbridge.common.util.AuditLogger;
import com.northbridge.common.util.BankDates;
import com.northbridge.payments.config.CoreBankingProperties;
import com.northbridge.payments.core.CoreAccount;
import com.northbridge.payments.core.CoreBankingClient;
import com.northbridge.payments.core.CoreBankingException;
import com.northbridge.payments.core.TransferRequest;
import com.northbridge.payments.core.TransferResponse;
import com.northbridge.payments.domain.Payment;
import com.northbridge.payments.domain.PaymentStatus;
import com.northbridge.payments.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.Date;
import java.util.List;

/**
 * Releases scheduled bill payments whose send-on date has arrived: checks available funds in
 * core-banking and posts the debit to the bill pay suspense GL. Payments that hit an I/O error stay
 * SCHEDULED and are picked up by the next run.
 */
@Component
public class BillPaymentReleaseJob {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentReleaseJob.class);

    private final PaymentRepository payments;
    private final CoreBankingClient core;
    private final CoreBankingProperties coreProps;

    public BillPaymentReleaseJob(PaymentRepository payments, CoreBankingClient core, CoreBankingProperties coreProps) {
        this.payments = payments;
        this.core = core;
        this.coreProps = coreProps;
    }

    @Scheduled(cron = "${nb.billpay.release-cron:0 */5 * * * *}", zone = "America/New_York")
    public void scheduledRun() {
        releaseDue();
    }

    @Transactional
    public int releaseDue() {
        Date businessDate = BankDates.businessDate(new Date());
        List<Payment> due = payments.findByStatusAndSendOnLessThanEqualOrderBySendOnAsc(PaymentStatus.SCHEDULED,
                businessDate);
        if (due.isEmpty()) {
            return 0;
        }
        log.info("bill pay release starting businessDate={} due={}", BankDates.formatDate(businessDate), due.size());
        int released = 0;
        for (Payment payment : due) {
            if (release(payment)) {
                released++;
            }
        }
        log.info("bill pay release finished released={} of {}", released, due.size());
        return released;
    }

    private boolean release(Payment payment) {
        try {
            CoreAccount account = core.getAccount(payment.getFromAccount());
            if (!account.isOpen()) {
                fail(payment, "ACCOUNT_NOT_OPEN");
                return false;
            }
            if (payment.getAmount().isGreaterThan(Money.of(account.getAvailableBalance()))) {
                fail(payment, "NSF");
                return false;
            }
            TransferResponse posted = core.postTransfer(new TransferRequest(
                    payment.getFromAccount(),
                    coreProps.getSuspenseAccount(),
                    payment.getAmount().getAmount().toPlainString(),
                    "BILLPAY " + payment.getPaymentId() + " " + payment.getPayeeId(),
                    payment.getPaymentId()));
            markReleased(payment, posted.getTransferId());
            return true;
        } catch (CoreBankingException e) {
            if ("DUPLICATE".equals(e.getCode())) {
                log.info("bill payment already posted paymentId={}", payment.getPaymentId());
                markReleased(payment, payment.getCoreTransferId());
                return true;
            }
            fail(payment, e.getCode());
            return false;
        } catch (ResourceAccessException e) {
            log.warn("bill payment release deferred paymentId={}: {}", payment.getPaymentId(), e.getMessage());
            return false;
        }
    }

    private void markReleased(Payment payment, String transferId) {
        payment.setStatus(PaymentStatus.RELEASED);
        payment.setCoreTransferId(transferId);
        payment.setReleasedAt(new Date());
        log.info("bill payment released paymentId={} transferId={}", payment.getPaymentId(), transferId);
        AuditLogger.record("AUD-BP-RELEASE", "system", "paymentId=" + payment.getPaymentId());
    }

    private void fail(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        log.warn("bill payment failed paymentId={} reason={}", payment.getPaymentId(), reason);
        AuditLogger.record("AUD-BP-FAIL", "system", "paymentId=" + payment.getPaymentId() + " reason=" + reason);
    }
}
