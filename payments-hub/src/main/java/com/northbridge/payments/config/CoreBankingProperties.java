package com.northbridge.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection settings for the core-banking posting API.
 */
@ConfigurationProperties(prefix = "core.banking")
public class CoreBankingProperties {

    private String baseUrl;
    private String username;
    private String password;
    private int connectTimeoutMs = 1000;
    private int readTimeoutMs = 2000;
    private int maxAttempts = 3;
    private long backoffMs = 250;
    /** GL suspense account that receives bill pay and P2P debits before settlement. */
    private String suspenseAccount = "307000555010";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public long getBackoffMs() { return backoffMs; }
    public void setBackoffMs(long backoffMs) { this.backoffMs = backoffMs; }
    public String getSuspenseAccount() { return suspenseAccount; }
    public void setSuspenseAccount(String suspenseAccount) { this.suspenseAccount = suspenseAccount; }
}
