package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.Wallet;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;
import org.example.dto.request.UpdateWalletRequest;
import org.example.dto.response.WalletResponse;
import org.example.mapper.WalletMapper;
import org.example.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @GetMapping
    public List<WalletResponse> findAllWallets() {
        List<Wallet> wallets = walletService.findAllWallets();
        return walletMapper.toResponseList(wallets);
    }

    @GetMapping("/{walletId}")
    public WalletResponse findWalletById(@PathVariable Long walletId) {
        Wallet wallet = walletService.findWalletById(walletId);
        return walletMapper.toResponse(wallet);
    }

    @GetMapping("/user/{userId}")
    public List<WalletResponse> findUserWallets(@PathVariable Long userId) {
        List<Wallet> wallets = walletService.findUserWallets(userId);
        return walletMapper.toResponseList(wallets);
    }

    @GetMapping("/address/{walletAddress}")
    public WalletResponse findWalletByAddress(@PathVariable String walletAddress) {
        Wallet wallet = walletService.findWalletByAddress(walletAddress);
        return walletMapper.toResponse(wallet);
    }

    @GetMapping("/address/{walletAddress}/exists")
    public boolean walletExists(@PathVariable String walletAddress) {
        return walletService.walletExists(walletAddress);
    }

    @GetMapping("/network/{network}")
    public List<WalletResponse> findWalletsByNetwork(@PathVariable BlockchainNetwork network) {
        List<Wallet> wallets = walletService.findWalletsByNetwork(network);
        return walletMapper.toResponseList(wallets);
    }

    @GetMapping("/provider/{provider}")
    public List<WalletResponse> findWalletsByProvider(@PathVariable WalletProvider provider) {
        List<Wallet> wallets = walletService.findWalletsByProvider(provider);
        return walletMapper.toResponseList(wallets);
    }

    @PatchMapping("/{walletId}")
    public void updateWalletInfo(
            @PathVariable Long walletId,
            @Valid @RequestBody UpdateWalletRequest request
    ) {
        walletService.updateWalletInfo(
                walletId,
                request.walletAddress(),
                request.network(),
                request.provider()
        );
    }
}