package org.example.dto.response;

import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long buyerId,
        String buyerUsername,
        Long sellerId,
        String sellerUsername,
        Long listingId,
        String listingTitle,
        BigDecimal totalPrice,
        String currency,
        BlockchainNetwork network,
        OrderStatus status,
        String escrowContract,
        String escrowDealId,
        String paymentTxHash,
        String releaseTxHash,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        LocalDateTime sellerMarkedDoneAt,
        LocalDateTime completedAt
) {
}