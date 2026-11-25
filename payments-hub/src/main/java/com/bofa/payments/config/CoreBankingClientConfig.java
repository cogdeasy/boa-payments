package com.bofa.payments.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class CoreBankingClientConfig {

    private static final Logger log = LoggerFactory.getLogger(CoreBankingClientConfig.class);

    @Bean
    public RestTemplate coreBankingRestTemplate(RestTemplateBuilder builder, CoreBankingProperties props) {
        return builder
                .rootUri(props.getBaseUrl())
                .basicAuthentication(props.getUsername(), props.getPassword())
                .setConnectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .build();
    }

    /**
     * Retries core-banking calls that fail with an I/O error (connect refused, read timed out).
     * Business rejections (4xx) are never retried.
     */
    @Bean
    public RetryTemplate coreBankingRetryTemplate(CoreBankingProperties props) {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(props.getMaxAttempts(),
                Collections.singletonMap(ResourceAccessException.class, true)));
        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(props.getBackoffMs());
        template.setBackOffPolicy(backOff);
        template.registerListener(new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                         Throwable throwable) {
                log.warn("core-banking call failed (attempt {}/{}): {}", context.getRetryCount(),
                        props.getMaxAttempts(), throwable.getMessage());
            }
        });
        return template;
    }
}
