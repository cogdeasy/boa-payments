package com.northbridge.payments.service;

import com.northbridge.common.util.BankDates;
import com.northbridge.payments.core.CoreBankingClient;
import com.northbridge.payments.repository.PaymentRepository;
import com.northbridge.payments.web.dto.BillPaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BillPayServiceTest {

    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final CoreBankingClient core = mock(CoreBankingClient.class);
    private final BillPayService service = new BillPayService(payments, mock(PayeeService.class), core);

    @Test
    void rejectsSendOnInThePast() {
        BillPaymentRequest request = request("50.00",
                BankDates.formatDate(BankDates.addDays(BankDates.businessDate(new Date()), -1)));

        PaymentRejectedException e = catchThrowableOfType(() -> service.schedule(request, "dana.whitfield"),
                PaymentRejectedException.class);

        assertThat(e.getCode()).isEqualTo("INVALID_SEND_ON");
        verifyNoInteractions(core, payments);
    }

    @Test
    void rejectsAmountOverBillPayLimit() {
        BillPaymentRequest request = request("25000.01", BankDates.formatDate(new Date()));

        PaymentRejectedException e = catchThrowableOfType(() -> service.schedule(request, "dana.whitfield"),
                PaymentRejectedException.class);

        assertThat(e.getCode()).isEqualTo("INVALID_AMOUNT");
    }

    private static BillPaymentRequest request(String amount, String sendOn) {
        BillPaymentRequest r = new BillPaymentRequest();
        r.setCif("100000001");
        r.setFromAccount("104000123455");
        r.setPayeeId("PY-1001");
        r.setAmount(amount);
        r.setSendOn(sendOn);
        return r;
    }
}
