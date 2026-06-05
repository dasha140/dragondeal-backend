package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.PaymentIntent;
import org.example.dto.request.CreatePaymentIntentRequest;
import org.example.dto.response.PaymentIntentResponse;
import org.example.mapper.PaymentIntentMapper;
import org.example.service.CurrentUserService;
import org.example.service.PaymentIntentService;
import org.example.dto.request.SubmitPaymentIntentTxRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-intents")
@RequiredArgsConstructor
public class PaymentIntentController {

    private final PaymentIntentService paymentIntentService;
    private final PaymentIntentMapper paymentIntentMapper;
    private final CurrentUserService currentUserService;

    @PostMapping
    public PaymentIntentResponse createPaymentIntent(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreatePaymentIntentRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);

        PaymentIntent paymentIntent = paymentIntentService.createPaymentIntent(
                currentUser,
                request.orderId(),
                request.paymentMethodId()
        );

        return paymentIntentMapper.toResponse(paymentIntent);
    }

    @GetMapping("/{paymentIntentId}")
    public PaymentIntentResponse findById(@PathVariable Long paymentIntentId) {
        PaymentIntent paymentIntent = paymentIntentService.findById(paymentIntentId);
        return paymentIntentMapper.toResponse(paymentIntent);
    }

    @PatchMapping("/{paymentIntentId}/tx")
    public PaymentIntentResponse submitBlockchainPaymentTx(
            HttpServletRequest httpRequest,
            @PathVariable Long paymentIntentId,
            @Valid @RequestBody SubmitPaymentIntentTxRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);

        PaymentIntent paymentIntent = paymentIntentService.submitBlockchainPaymentTx(
                currentUser,
                paymentIntentId,
                request.escrowDealId(),
                request.paymentTxHash()
        );

        return paymentIntentMapper.toResponse(paymentIntent);
    }

    @PatchMapping("/{paymentIntentId}/verify")
    public PaymentIntentResponse verifyBlockchainPayment(
            @PathVariable Long paymentIntentId
    ) {
        PaymentIntent paymentIntent = paymentIntentService.verifyBlockchainPayment(paymentIntentId);
        return paymentIntentMapper.toResponse(paymentIntent);
    }

    @GetMapping("/order/{orderId}")
    public PaymentIntentResponse findByOrderId(@PathVariable Long orderId) {
        PaymentIntent paymentIntent = paymentIntentService.findByOrderId(orderId);
        return paymentIntentMapper.toResponse(paymentIntent);
    }
}