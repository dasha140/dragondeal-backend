package org.example.dto.response;

import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.TransactionStatus;
import org.example.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long orderId,
        Long userId,
        String username,
        String txHash,
        String fromWallet,
        String toWallet,
        BigDecimal amount,
        String currency,
        BlockchainNetwork network,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt
) {
}