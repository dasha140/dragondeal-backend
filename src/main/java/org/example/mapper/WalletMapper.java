package org.example.mapper;

import org.example.domain.Wallet;
import org.example.dto.response.WalletResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getUser().getUsername(),
                wallet.getWalletAddress(),
                wallet.getNetwork(),
                wallet.getProvider(),
                wallet.getCreatedAt()
        );
    }

    public List<WalletResponse> toResponseList(List<Wallet> wallets) {
        return wallets.stream()
                .map(this::toResponse)
                .toList();
    }
}