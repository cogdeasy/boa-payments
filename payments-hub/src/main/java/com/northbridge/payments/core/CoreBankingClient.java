package com.northbridge.payments.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northbridge.common.util.AccountNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Thin client over the core-banking account and posting API ({@code /api/v1}).
 */
@Component
public class CoreBankingClient {

    private static final Logger log = LoggerFactory.getLogger(CoreBankingClient.class);

    private final RestTemplate rest;
    private final ObjectMapper mapper;

    public CoreBankingClient(RestTemplate coreBankingRestTemplate, ObjectMapper mapper) {
        this.rest = coreBankingRestTemplate;
        this.mapper = mapper;
    }

    public CoreAccount getAccount(String accountNumber) {
        try {
            return rest.getForObject("/accounts/{n}", CoreAccount.class, accountNumber);
        } catch (HttpStatusCodeException e) {
            throw toCoreException(e);
        }
    }

    public TransferResponse postTransfer(TransferRequest request) {
        long start = System.currentTimeMillis();
        log.info("core POST /transfers from={} to={} amount={} key={}", AccountNumbers.mask(request.getFromAccount()),
                AccountNumbers.mask(request.getToAccount()), request.getAmount(), request.getIdempotencyKey());
        try {
            TransferResponse response = rest.postForObject("/transfers", request, TransferResponse.class);
            log.info("core transfer posted transferId={} key={} durationMs={}", response.getTransferId(),
                    request.getIdempotencyKey(), System.currentTimeMillis() - start);
            return response;
        } catch (HttpStatusCodeException e) {
            throw toCoreException(e);
        }
    }

    private CoreBankingException toCoreException(HttpStatusCodeException e) {
        String code = "CORE_" + e.getRawStatusCode();
        String message = e.getStatusText();
        try {
            JsonNode body = mapper.readTree(e.getResponseBodyAsString());
            if (body.hasNonNull("code")) {
                code = body.get("code").asText();
            }
            if (body.hasNonNull("message")) {
                message = body.get("message").asText();
            }
        } catch (Exception ignored) {
            // non-JSON error page from the gateway; keep the status line
        }
        return new CoreBankingException(e.getRawStatusCode(), code, message);
    }
}
