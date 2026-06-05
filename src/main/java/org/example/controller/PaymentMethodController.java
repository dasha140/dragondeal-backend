package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.PaymentMethod;
import org.example.dto.response.PaymentMethodResponse;
import org.example.mapper.PaymentMethodMapper;
import org.example.service.PaymentMethodService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.example.dto.request.UpdatePaymentMethodContractRequest;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final PaymentMethodMapper paymentMethodMapper;

    @GetMapping
    public List<PaymentMethodResponse> findEnabledPaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodService.findEnabledPaymentMethods();
        return paymentMethodMapper.toResponseList(paymentMethods);
    }

    @GetMapping("/all")
    public List<PaymentMethodResponse> findAllPaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodService.findAllPaymentMethods();
        return paymentMethodMapper.toResponseList(paymentMethods);
    }
    @PatchMapping("/{paymentMethodId}/contracts")
    public PaymentMethodResponse updateContracts(
            @PathVariable Long paymentMethodId,
            @Valid @RequestBody UpdatePaymentMethodContractRequest request
    ) {
        PaymentMethod paymentMethod = paymentMethodService.updateContracts(
                paymentMethodId,
                request.tokenContract(),
                request.escrowContract(),
                request.enabled()
        );

        return paymentMethodMapper.toResponse(paymentMethod);
    }
}