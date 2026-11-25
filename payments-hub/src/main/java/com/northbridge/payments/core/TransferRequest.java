package com.northbridge.payments.core;

/** Body of core-banking {@code POST /transfers}. */
public class TransferRequest {

    private String fromAccount;
    private String toAccount;
    private String amount;
    private String memo;
    private String idempotencyKey;

    public TransferRequest() {
    }

    public TransferRequest(String fromAccount, String toAccount, String amount, String memo, String idempotencyKey) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.memo = memo;
        this.idempotencyKey = idempotencyKey;
    }

    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
