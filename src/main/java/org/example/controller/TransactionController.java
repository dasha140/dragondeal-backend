package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.WalletTransaction;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.TransactionStatus;
import org.example.domain.enums.TransactionType;
import org.example.dto.response.TransactionResponse;
import org.example.mapper.TransactionMapper;
import org.example.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.example.dto.request.UpdateTransactionHashRequest;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping
    public List<TransactionResponse> findAllTransactions() {
        List<WalletTransaction> transactions = transactionService.findAllTransactions();
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse findTransactionById(@PathVariable Long transactionId) {
        WalletTransaction transaction = transactionService.findTransactionById(transactionId);
        return transactionMapper.toResponse(transaction);
    }

    @GetMapping("/user/{userId}")
    public List<TransactionResponse> findUserTransactions(@PathVariable Long userId) {
        List<WalletTransaction> transactions = transactionService.findUserTransactions(userId);
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/order/{orderId}")
    public List<TransactionResponse> findOrderTransactions(@PathVariable Long orderId) {
        List<WalletTransaction> transactions = transactionService.findOrderTransactions(orderId);
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/tx/{txHash}")
    public TransactionResponse findByTxHash(@PathVariable String txHash) {
        WalletTransaction transaction = transactionService.findByTxHash(txHash);
        return transactionMapper.toResponse(transaction);
    }

    @GetMapping("/tx/{txHash}/exists")
    public boolean transactionExists(@PathVariable String txHash) {
        return transactionService.transactionExists(txHash);
    }

    @GetMapping("/status/{status}")
    public List<TransactionResponse> findByStatus(@PathVariable TransactionStatus status) {
        List<WalletTransaction> transactions = transactionService.findByStatus(status);
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/type/{type}")
    public List<TransactionResponse> findByType(@PathVariable TransactionType type) {
        List<WalletTransaction> transactions = transactionService.findByType(type);
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/network/{network}")
    public List<TransactionResponse> findByNetwork(@PathVariable BlockchainNetwork network) {
        List<WalletTransaction> transactions = transactionService.findByNetwork(network);
        return transactionMapper.toResponseList(transactions);
    }

    @GetMapping("/currency/{currency}")
    public List<TransactionResponse> findByCurrency(@PathVariable String currency) {
        List<WalletTransaction> transactions = transactionService.findByCurrency(currency);
        return transactionMapper.toResponseList(transactions);
    }

    @PatchMapping("/{transactionId}/confirm")
    public void markTransactionConfirmed(@PathVariable Long transactionId) {
        transactionService.markTransactionConfirmed(transactionId);
    }

    @PatchMapping("/{transactionId}/fail")
    public void markTransactionFailed(@PathVariable Long transactionId) {
        transactionService.markTransactionFailed(transactionId);
    }

    @PatchMapping("/{transactionId}/tx-hash/pending")
    public void updateTxHashAndMarkPending(
            @PathVariable Long transactionId,
            @Valid @RequestBody UpdateTransactionHashRequest request
    ) {
        transactionService.updateTxHashAndMarkPending(transactionId, request.txHash());
    }

    @PatchMapping("/{transactionId}/tx-hash/confirmed")
    public void updateTxHashAndMarkConfirmed(
            @PathVariable Long transactionId,
            @Valid @RequestBody UpdateTransactionHashRequest request
    ) {
        transactionService.updateTxHashAndMarkConfirmed(transactionId, request.txHash());
    }
}