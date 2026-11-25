package com.northbridge.payments.web;

import com.northbridge.payments.domain.Payment;
import com.northbridge.payments.export.RemittanceExportService;
import com.northbridge.payments.job.BillPaymentReleaseJob;
import com.northbridge.payments.repository.PaymentHistoryDao;
import com.northbridge.payments.security.CustomerAccess;
import com.northbridge.payments.service.BillPayService;
import com.northbridge.payments.service.P2pService;
import com.northbridge.payments.service.PayeeService;
import com.northbridge.payments.web.dto.BillPaymentRequest;
import com.northbridge.payments.web.dto.NewPayeeRequest;
import com.northbridge.payments.web.dto.P2pRequest;
import com.northbridge.payments.web.dto.PayeeView;
import com.northbridge.payments.web.dto.PaymentResponse;
import com.northbridge.payments.web.dto.PaymentView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Api(tags = "payments")
public class PaymentsController {

    private final PayeeService payeeService;
    private final BillPayService billPayService;
    private final P2pService p2pService;
    private final PaymentHistoryDao historyDao;
    private final RemittanceExportService exportService;
    private final BillPaymentReleaseJob releaseJob;
    private final CustomerAccess access;

    public PaymentsController(PayeeService payeeService, BillPayService billPayService, P2pService p2pService,
                              PaymentHistoryDao historyDao, RemittanceExportService exportService,
                              BillPaymentReleaseJob releaseJob, CustomerAccess access) {
        this.payeeService = payeeService;
        this.billPayService = billPayService;
        this.p2pService = p2pService;
        this.historyDao = historyDao;
        this.exportService = exportService;
        this.releaseJob = releaseJob;
        this.access = access;
    }

    @GetMapping("/payees")
    @ApiOperation("List a customer's active bill pay payees")
    public List<PayeeView> payees(@RequestParam String cif) {
        return payeeService.listPayees(cif);
    }

    @PostMapping("/payees")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Add a bill pay payee")
    public PayeeView addPayee(@Valid @RequestBody NewPayeeRequest request, Authentication auth) {
        access.assertCanAct(auth, request.getCif());
        return payeeService.addPayee(request, auth.getName());
    }

    @DeleteMapping("/payees/{payeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Remove a bill pay payee")
    public void removePayee(@PathVariable String payeeId, @RequestParam String cif, Authentication auth) {
        access.assertCanAct(auth, cif);
        payeeService.removePayee(cif, payeeId, auth.getName());
    }

    @PostMapping("/bill")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Schedule a bill payment")
    public PaymentResponse scheduleBill(@Valid @RequestBody BillPaymentRequest request, Authentication auth) {
        access.assertCanAct(auth, request.getCif());
        Payment payment = billPayService.schedule(request, auth.getName());
        return new PaymentResponse(payment.getPaymentId(), payment.getStatus().name());
    }

    @PostMapping("/p2p")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Send money to a person by email or mobile number")
    public PaymentResponse sendP2p(@Valid @RequestBody P2pRequest request, Authentication auth) {
        Payment payment = p2pService.send(request, auth.getName());
        return new PaymentResponse(payment.getPaymentId(), payment.getStatus().name());
    }

    @GetMapping("/history")
    @ApiOperation("Payment activity for a customer, newest first")
    public List<PaymentView> history(@RequestParam String cif,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String sort) {
        return historyDao.findHistory(cif, status, sort);
    }

    @GetMapping(value = "/history/export", produces = MediaType.APPLICATION_XML_VALUE)
    @ApiOperation("Payment activity as an STMT-XML 2.1 remittance advice")
    public String exportHistory(@RequestParam String cif, Authentication auth) {
        access.assertCanAct(auth, cif);
        return exportService.exportXml(cif);
    }

    @PostMapping("/ops/release-bills")
    @ApiOperation("Run the bill payment release job now (OPS)")
    public Map<String, Integer> releaseBills() {
        return Collections.singletonMap("released", releaseJob.releaseDue());
    }
}
