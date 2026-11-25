package com.northbridge.payments.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A biller a customer pays through bill pay. The biller account number is stored as a TKN-1 token
 * (see nb-common LegacyTokenCipher) and only the last four digits are shown back to the customer.
 */
@Entity
@Table(name = "PAYEE")
public class Payee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 16)
    private String payeeId;

    @Column(nullable = false, length = 9)
    private String cif;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 16)
    private String category;

    @Column(nullable = false, length = 64)
    private String accountToken;

    @Column(nullable = false, length = 4)
    private String accountLast4;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String payeeId) { this.payeeId = payeeId; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAccountToken() { return accountToken; }
    public void setAccountToken(String accountToken) { this.accountToken = accountToken; }
    public String getAccountLast4() { return accountLast4; }
    public void setAccountLast4(String accountLast4) { this.accountLast4 = accountLast4; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
