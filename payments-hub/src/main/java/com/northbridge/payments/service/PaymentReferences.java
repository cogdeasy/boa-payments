package com.northbridge.payments.service;

import com.northbridge.common.util.BankDates;

import java.util.Date;

/** Customer-facing references: {@code BP-20261125-000123}, {@code P2P-20261125-000124}, {@code PY-1001}. */
public final class PaymentReferences {

    private PaymentReferences() {
    }

    public static String paymentId(String prefix, Date businessDate, long sequence) {
        return String.format("%s-%s-%06d", prefix, BankDates.formatDate(businessDate).replace("-", ""), sequence);
    }

    public static String payeeId(long sequence) {
        return "PY-" + (1000 + sequence);
    }
}
