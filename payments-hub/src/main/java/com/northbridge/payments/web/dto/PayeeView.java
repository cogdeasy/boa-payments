package com.northbridge.payments.web.dto;

public class PayeeView {

    private final String payeeId;
    private final String name;
    private final String category;
    private final String accountRef;

    public PayeeView(String payeeId, String name, String category, String accountRef) {
        this.payeeId = payeeId;
        this.name = name;
        this.category = category;
        this.accountRef = accountRef;
    }

    public String getPayeeId() { return payeeId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getAccountRef() { return accountRef; }
}
