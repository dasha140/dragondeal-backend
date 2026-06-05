package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.Wallet;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<Wallet> findAllWallets() {
        return walletRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Wallet findWalletById(Long walletId) {
        return walletRepository.findWithUserById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));
    }

    @Transactional(readOnly = true)
    public List<Wallet> findUserWallets(Long userId) {
        return walletRepository.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public Wallet findWalletByAddress(String walletAddress) {
        return walletRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletAddress));
    }

    @Transactional(readOnly = true)
    public boolean walletExists(String walletAddress) {
        return walletRepository.existsByWalletAddress(walletAddress);
    }

    @Transactional(readOnly = true)
    public List<Wallet> findWalletsByNetwork(BlockchainNetwork network) {
        return walletRepository.findByNetwork(network);
    }

    @Transactional(readOnly = true)
    public List<Wallet> findWalletsByProvider(WalletProvider provider) {
        return walletRepository.findByProvider(provider);
    }

    @Transactional
    public void updateWalletInfo(
            Long walletId,
            String walletAddress,
            BlockchainNetwork network,
            WalletProvider provider
    ) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new IllegalArgumentException("Wallet address is required");
        }

        int updatedRows = walletRepository.updateWalletInfoById(
                walletId,
                walletAddress,
                network,
                provider
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Wallet not found: " + walletId);
        }
    }
}