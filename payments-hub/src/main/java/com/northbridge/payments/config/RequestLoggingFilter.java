package com.northbridge.payments.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Puts the edge request id into the MDC ({@code rid}) and writes one completion line per API call,
 * which the log pipeline uses for the per-endpoint error-rate and latency dashboards.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("http");
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String rid = request.getHeader(REQUEST_ID_HEADER);
        if (rid == null || rid.isEmpty()) {
            rid = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put("rid", rid);
        response.setHeader(REQUEST_ID_HEADER, rid);
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                log.info("{} {} status={} durationMs={}", request.getMethod(), request.getRequestURI(),
                        response.getStatus(), System.currentTimeMillis() - start);
            }
            MDC.remove("rid");
        }
    }
}
