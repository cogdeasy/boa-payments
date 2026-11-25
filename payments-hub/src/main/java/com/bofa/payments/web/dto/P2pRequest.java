package com.bofa.payments.web.dto;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class P2pRequest {

    @NotBlank
    @Pattern(regexp = "\\d{9}")
    private String cif;

    @NotBlank
    private String fromAccount;

    @NotNull
    @Valid
    private Recipient recipient;

    @NotBlank
    @Pattern(regexp = "\\d{1,7}(\\.\\d{1,2})?")
    private String amount;

    @Size(max = 140)
    private String memo;

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    public Recipient getRecipient() { return recipient; }
    public void setRecipient(Recipient recipient) { this.recipient = recipient; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public static class Recipient {

        @Email
        private String email;

        @Pattern(regexp = "\\d{10}")
        private String phone;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
