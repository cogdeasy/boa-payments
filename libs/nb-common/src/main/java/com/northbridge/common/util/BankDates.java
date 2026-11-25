package com.northbridge.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Business-date helpers. The posting engine runs on Eastern time; the "business date" rolls at the
 * EOD cut-off (20:00 ET), not at midnight.
 */
public final class BankDates {

    public static final TimeZone BANK_TZ = TimeZone.getTimeZone("America/New_York");
    public static final int EOD_CUTOFF_HOUR = 20;

    private static final SimpleDateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ISO_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        ISO_DATE.setTimeZone(BANK_TZ);
        ISO_TIMESTAMP.setTimeZone(BANK_TZ);
    }

    private BankDates() {
    }

    public static String formatDate(Date date) {
        return ISO_DATE.format(date);
    }

    public static String formatTimestamp(Date date) {
        return ISO_TIMESTAMP.format(date);
    }

    public static Date parseDate(String value) {
        try {
            return ISO_DATE.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date: " + value, e);
        }
    }

    /** Business date for a posting made at {@code when}; postings after the cut-off belong to the next day. */
    public static Date businessDate(Date when) {
        Calendar cal = Calendar.getInstance(BANK_TZ);
        cal.setTime(when);
        if (cal.get(Calendar.HOUR_OF_DAY) >= EOD_CUTOFF_HOUR) {
            cal.add(Calendar.DATE, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date startOfMonth(Date when) {
        Calendar cal = Calendar.getInstance(BANK_TZ);
        cal.setTime(when);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date addDays(Date when, int days) {
        Calendar cal = Calendar.getInstance(BANK_TZ);
        cal.setTime(when);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
}
