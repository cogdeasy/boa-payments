package com.bofa.payments.domain;

public enum PaymentStatus {
    /** Bill payment accepted, waiting for its send-on date. */
    SCHEDULED,
    /** Posting to core-banking in progress. */
    PROCESSING,
    /** Bill payment posted to core-banking by the release job. */
    RELEASED,
    /** P2P payment posted to core-banking. */
    SENT,
    FAILED,
    CANCELLED
}
