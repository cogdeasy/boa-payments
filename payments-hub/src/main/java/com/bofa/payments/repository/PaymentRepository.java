package com.bofa.payments.repository;

import com.bofa.common.money.Money;
import com.bofa.payments.domain.Payment;
import com.bofa.payments.domain.PaymentStatus;
import com.bofa.payments.domain.PaymentType;
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
            + "and p.status = com.bofa.payments.domain.PaymentStatus.SENT and p.createdAt >= :since")
    Money sumSentSince(@Param("cif") String cif, @Param("type") PaymentType type, @Param("since") Date since);
}
