package com.northbridge.payments.export;

import com.northbridge.common.util.BankDates;
import com.northbridge.common.xml.StatementXml;
import com.northbridge.payments.repository.PaymentHistoryDao;
import com.northbridge.payments.web.dto.PaymentView;
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
