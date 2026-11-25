package com.bofa.payments.web;

import com.bofa.payments.core.CoreAccount;
import com.bofa.payments.core.CoreBankingClient;
import com.bofa.common.util.BankDates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "boa.billpay.release-cron=-")
@AutoConfigureMockMvc
class PaymentsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CoreBankingClient core;

    @Test
    void payeesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/payments/payees").param("cif", "100000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerListsOwnPayees() throws Exception {
        mvc.perform(get("/api/v1/payments/payees").param("cif", "100000001")
                        .with(httpBasic("dana.whitfield", "Welcome1!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payeeId").value("PY-1001"))
                .andExpect(jsonPath("$[0].name").value("Con Edison"))
                .andExpect(jsonPath("$[0].accountRef").value("****7781"));
    }

    @Test
    void customerSchedulesBillPayment() throws Exception {
        CoreAccount account = new CoreAccount();
        account.setAccountNumber("104000123455");
        account.setCif("100000001");
        account.setStatus("OPEN");
        account.setAvailableBalance("4712.33");
        given(core.getAccount("104000123455")).willReturn(account);

        mvc.perform(post("/api/v1/payments/bill")
                        .with(httpBasic("dana.whitfield", "Welcome1!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cif\":\"100000001\",\"fromAccount\":\"104000123455\",\"payeeId\":\"PY-1001\","
                                + "\"amount\":\"125.40\",\"sendOn\":\"" + inAWeek() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(org.hamcrest.Matchers.startsWith("BP-")))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    private static String inAWeek() {
        return BankDates.formatDate(BankDates.addDays(BankDates.businessDate(new Date()), 7));
    }

    @Test
    void customerCannotScheduleBillForAnotherCustomer() throws Exception {
        mvc.perform(post("/api/v1/payments/bill")
                        .with(httpBasic("dana.whitfield", "Welcome1!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cif\":\"100000002\",\"fromAccount\":\"211000048218\",\"payeeId\":\"PY-1004\","
                                + "\"amount\":\"10.00\",\"sendOn\":\"2026-12-01\"}"))
                .andExpect(status().isForbidden());
    }
}
