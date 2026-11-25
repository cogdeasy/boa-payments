package com.northbridge.payments.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * Authenticated online-banking user. {@code cif} is null for staff (TELLER, OPS).
 */
public class NorthbridgeUser extends User {

    private static final long serialVersionUID = 1L;

    private final String cif;
    private final String role;

    public NorthbridgeUser(String username, String password, String role, String cif) {
        super(username, password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        this.cif = cif;
        this.role = role;
    }

    public String getCif() { return cif; }

    public String getRole() { return role; }

    public boolean isStaff() {
        return "TELLER".equals(role) || "OPS".equals(role);
    }
}
