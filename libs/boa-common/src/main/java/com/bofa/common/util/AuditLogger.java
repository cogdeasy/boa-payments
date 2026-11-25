package com.bofa.common.util;

import org.apache.log4j.Logger;

/**
 * Writes the SOX audit trail (AUD-* events) to the dedicated log4j "AUDIT" category, which the
 * platform team ships to the records archive.
 */
public final class AuditLogger {

    private static final Logger AUDIT = Logger.getLogger("AUDIT");

    private AuditLogger() {
    }

    public static void record(String eventCode, String actor, String detail) {
        AUDIT.info(eventCode + " actor=" + actor + " " + detail);
    }
}
