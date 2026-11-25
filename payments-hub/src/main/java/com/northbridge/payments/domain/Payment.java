package com.northbridge.payments.domain;

import com.northbridge.common.money.Money;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 32)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private PaymentType type;

    @Column(nullable = false, length = 9)
    private String cif;

    @Column(nullable = false, length = 12)
    private String fromAccount;

    @Column(length = 16)
    private String payeeId;

    @Column(length = 254)
    private String recipientEmail;

    @Column(length = 16)
    private String recipientPhone;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 15, scale = 2)
    private Money amount;

    @Column(length = 140)
    private String memo;

    @Temporal(TemporalType.DATE)
    private Date sendOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private PaymentStatus status;

    @Column(length = 32)
    private String coreTransferId;

    @Column(length = 64)
    private String failureReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date releasedAt;

    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String payeeId) { this.payeeId = payeeId; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public Money getAmount() { return amount; }
    public void setAmount(Money amount) { this.amount = amount; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public Date getSendOn() { return sendOn; }
    public void setSendOn(Date sendOn) { this.sendOn = sendOn; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getCoreTransferId() { return coreTransferId; }
    public void setCoreTransferId(String coreTransferId) { this.coreTransferId = coreTransferId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Date releasedAt) { this.releasedAt = releasedAt; }
}
