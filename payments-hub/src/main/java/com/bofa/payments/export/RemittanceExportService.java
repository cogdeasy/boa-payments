package com.bofa.payments.export;

import com.bofa.common.util.BankDates;
import com.bofa.common.xml.StatementXml;
import com.bofa.payments.repository.PaymentHistoryDao;
import com.bofa.payments.web.dto.PaymentView;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RemittanceExportService {

    private final PaymentHistoryDao history;

    public RemittanceExportService(PaymentHistoryDao history) {
        this.history = history;
    }

    public String exportXml(String cif) {
        RemittanceAdvice advice = new RemittanceAdvice();
        advice.setCif(cif);
        advice.setGeneratedAt(BankDates.formatTimestamp(new Date()));
        for (PaymentView p : history.findHistory(cif, null, null)) {
            advice.getPayments().add(new RemittanceAdvice.Line(p.getPaymentId(), p.getType(), p.getFromAccount(),
                    p.getPayeeId() != null ? p.getPayeeId() : p.getRecipient(), p.getAmount(), p.getSendOn(),
                    p.getStatus()));
        }
        return StatementXml.marshal(advice);
    }
}
