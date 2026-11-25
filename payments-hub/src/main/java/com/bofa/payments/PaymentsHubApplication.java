package com.bofa.payments;

import com.bofa.common.util.BankDates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class PaymentsHubApplication {

    static {
        // Posting, cut-off and send-on dates are all Eastern; keep the JVM aligned with the ledger.
        TimeZone.setDefault(BankDates.BANK_TZ);
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentsHubApplication.class, args);
    }
}
