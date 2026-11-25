package com.bofa.payments.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Entitlement checks for customer-initiated requests. Customers may only act for their own CIF;
 * tellers and operations staff may act for any customer.
 */
@Component
public class CustomerAccess {

    public void assertCanAct(Authentication auth, String cif) {
        BofaUser user = (BofaUser) auth.getPrincipal();
        if (user.isStaff()) {
            return;
        }
        if (cif == null || !cif.equals(user.getCif())) {
            throw new AccessDeniedException("Not entitled to act for this customer");
        }
    }

    public void assertOwnsAccount(Authentication auth, String accountCif) {
        assertCanAct(auth, accountCif);
    }
}
