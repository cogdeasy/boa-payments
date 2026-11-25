package com.northbridge.payments.repository;

import com.northbridge.common.money.Money;
import com.northbridge.payments.domain.Payment;
import com.northbridge.payments.domain.PaymentStatus;
import com.northbridge.payments.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByStatusAndSendOnLessThanEqualOrderBySendOnAsc(PaymentStatus status, Date sendOn);

    @Query("select sum(p.amount) from Payment p where p.cif = :cif and p.type = :type "
            + "and p.status = com.northbridge.payments.domain.PaymentStatus.SENT and p.createdAt >= :since")
    Money sumSentSince(@Param("cif") String cif, @Param("type") PaymentType type, @Param("since") Date since);
}
