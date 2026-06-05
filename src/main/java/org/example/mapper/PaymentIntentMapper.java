package org.example.mapper;

import org.example.domain.PaymentIntent;
import org.example.dto.response.PaymentIntentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentIntentMapper {

    public PaymentIntentResponse toResponse(PaymentIntent paymentIntent) {
        return new PaymentIntentResponse(
                paymentIntent.getId(),
                paymentIntent.getOrder().getId(),
                paymentIntent.getBuyerId(),
                paymentIntent.getSellerId(),
                paymentIntent.getPaymentMethod().getId(),
                paymentIntent.getPaymentMethod().getCurrency(),
                paymentIntent.getPaymentMethod().getNetwork(),
                paymentIntent.getAmount(),
                paymentIntent.getAmountAtomic(),
                paymentIntent.getChainId(),
                paymentIntent.getTokenContract(),
                paymentIntent.getEscrowContract(),
                paymentIntent.getEscrowDealId(),
                paymentIntent.getPaymentTxHash(),
                paymentIntent.getStatus(),
                paymentIntent.getCreatedAt(),
                paymentIntent.getExpiresAt(),
                paymentIntent.getPaidAt()
        );
    }
}