package com.bofa.payments.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment activity in the STMT-XML 2.1 remittance block, merged by the statements service into the
 * customer's monthly statement and sent to the print vendor.
 */
@XmlRootElement(name = "RemittanceAdvice")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceAdvice {

    @XmlAttribute
    private String schemaVersion = "2.1";

    @XmlElement
    private String cif;

    @XmlElement
    private String generatedAt;

    @XmlElementWrapper(name = "Payments")
    @XmlElement(name = "Payment")
    private List<Line> payments = new ArrayList<>();

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    public List<Line> getPayments() { return payments; }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Line {
        @XmlAttribute
        private String paymentId;
        @XmlElement
        private String type;
        @XmlElement
        private String account;
        @XmlElement
        private String payee;
        @XmlElement
        private String amount;
        @XmlElement
        private String sendOn;
        @XmlElement
        private String status;

        public Line() {
        }

        public Line(String paymentId, String type, String account, String payee, String amount, String sendOn,
                    String status) {
            this.paymentId = paymentId;
            this.type = type;
            this.account = account;
            this.payee = payee;
            this.amount = amount;
            this.sendOn = sendOn;
            this.status = status;
        }

        public String getPaymentId() { return paymentId; }
        public String getAmount() { return amount; }
    }
}
