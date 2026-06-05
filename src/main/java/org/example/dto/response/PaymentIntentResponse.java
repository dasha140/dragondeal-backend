package org.example.dto.response;

import org.example.domain.enums.CryptoCurrency;
import org.example.domain.enums.CryptoNetwork;
import org.example.domain.enums.PaymentIntentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentIntentResponse(
        Long id,
        Long orderId,
        Long buyerId,
        Long sellerId,
        Long paymentMethodId,
        CryptoCurrency currency,
        CryptoNetwork network,
        BigDecimal amount,
        String amountAtomic,
        Long chainId,
        String tokenContract,
        String escrowContract,
        String escrowDealId,
        String paymentTxHash,
        PaymentIntentStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime paidAt
) {
}