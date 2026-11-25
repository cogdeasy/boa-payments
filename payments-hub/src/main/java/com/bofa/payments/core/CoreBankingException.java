package com.bofa.payments.core;

/**
 * A business rejection from core-banking (NSF, LIMIT_EXCEEDED, ACCOUNT_NOT_FOUND, DUPLICATE).
 */
public class CoreBankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int httpStatus;
    private final String code;

    public CoreBankingException(int httpStatus, String code, String message) {
        super(code + ": " + message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() { return httpStatus; }

    public String getCode() { return code; }
}
