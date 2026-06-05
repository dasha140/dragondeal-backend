package org.example.dto.response;

import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;

import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long userId,
        String username,
        String walletAddress,
        BlockchainNetwork network,
        WalletProvider provider,
        LocalDateTime createdAt
) {
}