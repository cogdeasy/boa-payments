package com.bofa.payments.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Account as returned by core-banking {@code GET /accounts/{accountNumber}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreAccount {

    private String accountNumber;
    private String maskedNumber;
    private String cif;
    private String productCode;
    private String nickname;
    private String status;
    private String ledgerBalance;
    private String availableBalance;
    private String holdAmount;
    private String openedOn;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getMaskedNumber() { return maskedNumber; }
    public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(String ledgerBalance) { this.ledgerBalance = ledgerBalance; }
    public String getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(String availableBalance) { this.availableBalance = availableBalance; }
    public String getHoldAmount() { return holdAmount; }
    public void setHoldAmount(String holdAmount) { this.holdAmount = holdAmount; }
    public String getOpenedOn() { return openedOn; }
    public void setOpenedOn(String openedOn) { this.openedOn = openedOn; }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }
}
