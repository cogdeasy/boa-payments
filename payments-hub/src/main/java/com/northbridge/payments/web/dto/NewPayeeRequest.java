package com.northbridge.payments.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class NewPayeeRequest {

    @NotBlank
    @Pattern(regexp = "\\d{9}")
    private String cif;

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotBlank
    @Pattern(regexp = "UTILITY|CARD|TELECOM|INSURANCE|LOAN|OTHER")
    private String category;

    /** Account number at the biller, as printed on the customer's bill. */
    @NotBlank
    @Pattern(regexp = "[0-9A-Za-z-]{4,30}")
    private String accountNumber;

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
