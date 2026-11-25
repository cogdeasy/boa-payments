package com.northbridge.payments.service;

import com.northbridge.common.crypto.LegacyTokenCipher;
import com.northbridge.common.util.AuditLogger;
import com.northbridge.payments.domain.Payee;
import com.northbridge.payments.repository.PayeeRepository;
import com.northbridge.payments.web.dto.NewPayeeRequest;
import com.northbridge.payments.web.dto.PayeeView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayeeService {

    private final PayeeRepository payees;

    public PayeeService(PayeeRepository payees) {
        this.payees = payees;
    }

    @Transactional(readOnly = true)
    public List<PayeeView> listPayees(String cif) {
        return payees.findByCifAndActiveTrueOrderByNameAsc(cif).stream()
                .map(PayeeService::toView)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayeeView addPayee(NewPayeeRequest request, String actor) {
        Payee payee = new Payee();
        payee.setCif(request.getCif());
        payee.setName(request.getName().trim());
        payee.setCategory(request.getCategory());
        payee.setAccountToken(LegacyTokenCipher.encrypt(request.getAccountNumber()));
        String acct = request.getAccountNumber();
        payee.setAccountLast4(acct.substring(acct.length() - 4));
        payee = payees.save(payee);
        payee.setPayeeId(PaymentReferences.payeeId(payee.getId()));
        AuditLogger.record("AUD-PY-ADD", actor, "cif=" + payee.getCif() + " payeeId=" + payee.getPayeeId());
        return toView(payee);
    }

    @Transactional
    public void removePayee(String cif, String payeeId, String actor) {
        Payee payee = requireActivePayee(cif, payeeId);
        payee.setActive(false);
        AuditLogger.record("AUD-PY-DEL", actor, "cif=" + cif + " payeeId=" + payeeId);
    }

    @Transactional(readOnly = true)
    public Payee requireActivePayee(String cif, String payeeId) {
        return payees.findByPayeeId(payeeId)
                .filter(p -> p.isActive() && p.getCif().equals(cif))
                .orElseThrow(() -> new PaymentRejectedException("PAYEE_NOT_FOUND", "Unknown payee " + payeeId));
    }

    static PayeeView toView(Payee p) {
        return new PayeeView(p.getPayeeId(), p.getName(), p.getCategory(), "****" + p.getAccountLast4());
    }
}
