package com.bofa.payments.service;

/** The payment was refused for a business reason; mapped to 422 with the code. */
public class PaymentRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public PaymentRejectedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
