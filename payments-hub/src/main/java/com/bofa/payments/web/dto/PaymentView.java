package com.bofa.payments.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentView {

    private String paymentId;
    private String type;
    private String cif;
    private String fromAccount;
    private String payeeId;
    private String recipient;
    private String amount;
    private String memo;
    private String sendOn;
    private String status;
    private String coreTransferId;
    private String createdAt;

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String payeeId) { this.payeeId = payeeId; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getSendOn() { return sendOn; }
    public void setSendOn(String sendOn) { this.sendOn = sendOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCoreTransferId() { return coreTransferId; }
    public void setCoreTransferId(String coreTransferId) { this.coreTransferId = coreTransferId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
