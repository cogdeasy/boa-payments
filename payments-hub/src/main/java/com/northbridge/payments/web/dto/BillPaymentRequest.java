package com.northbridge.payments.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class BillPaymentRequest {

    @NotBlank
    @Pattern(regexp = "\\d{9}")
    private String cif;

    @NotBlank
    @Pattern(regexp = "\\d{12}")
    private String fromAccount;

    @NotBlank
    private String payeeId;

    @NotBlank
    @Pattern(regexp = "\\d{1,7}(\\.\\d{1,2})?")
    private String amount;

    /** yyyy-MM-dd, Eastern. */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String sendOn;

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String payeeId) { this.payeeId = payeeId; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getSendOn() { return sendOn; }
    public void setSendOn(String sendOn) { this.sendOn = sendOn; }
}
