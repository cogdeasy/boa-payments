package com.northbridge.common.util;

/**
 * Northbridge DDA account numbers are 12 digits: 3-digit branch, 8-digit serial, 1 mod-10 check digit.
 */
public final class AccountNumbers {

    private AccountNumbers() {
    }

    public static boolean isValid(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("\\d{12}")) {
            return false;
        }
        return checkDigit(accountNumber.substring(0, 11)) == Character.getNumericValue(accountNumber.charAt(11));
    }

    public static String withCheckDigit(String elevenDigits) {
        return elevenDigits + checkDigit(elevenDigits);
    }

    /** Masks all but the last four digits, e.g. {@code ********4821}. */
    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < accountNumber.length() - 4; i++) {
            sb.append('*');
        }
        return sb.append(accountNumber.substring(accountNumber.length() - 4)).toString();
    }

    static int checkDigit(String digits) {
        int sum = 0;
        boolean dbl = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = Character.getNumericValue(digits.charAt(i));
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
