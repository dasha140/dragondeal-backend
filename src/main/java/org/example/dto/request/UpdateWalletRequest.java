package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;

public record UpdateWalletRequest(

        @NotBlank(message = "Wallet address is required")
        @Size(max = 255, message = "Wallet address must be at most 255 characters")
        String walletAddress,

        @NotNull(message = "Network is required")
        BlockchainNetwork network,

        @NotNull(message = "Provider is required")
        WalletProvider provider
) {
}