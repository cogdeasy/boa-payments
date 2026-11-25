package com.bofa.payments.service;

/** core-banking did not answer in time after all retry attempts; mapped to 504. */
public class CoreUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CoreUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
