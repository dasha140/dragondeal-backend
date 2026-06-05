package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.WalletTransaction;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.TransactionStatus;
import org.example.domain.enums.TransactionType;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional(readOnly = true)
    public List<WalletTransaction> findAllTransactions() {
        return walletTransactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WalletTransaction findTransactionById(Long transactionId) {
        return walletTransactionRepository.findWithDetailsById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findUserTransactions(Long userId) {
        return walletTransactionRepository.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findOrderTransactions(Long orderId) {
        return walletTransactionRepository.findByOrder_Id(orderId);
    }

    @Transactional(readOnly = true)
    public WalletTransaction findByTxHash(String txHash) {
        return walletTransactionRepository.findByTxHash(txHash)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + txHash));
    }

    @Transactional(readOnly = true)
    public boolean transactionExists(String txHash) {
        return walletTransactionRepository.existsByTxHash(txHash);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findByStatus(TransactionStatus status) {
        return walletTransactionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findByType(TransactionType type) {
        return walletTransactionRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findByNetwork(BlockchainNetwork network) {
        return walletTransactionRepository.findByNetwork(network);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findByCurrency(String currency) {
        return walletTransactionRepository.findByCurrency(currency);
    }

    @Transactional
    public void markTransactionConfirmed(Long transactionId) {
        int updatedRows = walletTransactionRepository.updateStatusById(
                transactionId,
                TransactionStatus.CONFIRMED
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
    }

    @Transactional
    public void markTransactionFailed(Long transactionId) {
        int updatedRows = walletTransactionRepository.updateStatusById(
                transactionId,
                TransactionStatus.FAILED
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
    }

    @Transactional
    public void updateTxHashAndMarkPending(Long transactionId, String txHash) {
        if (txHash == null || txHash.isBlank()) {
            throw new IllegalArgumentException("Transaction hash is required");
        }

        int updatedRows = walletTransactionRepository.updateTxHashAndStatusById(
                transactionId,
                txHash,
                TransactionStatus.PENDING
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
    }

    @Transactional
    public void updateTxHashAndMarkConfirmed(Long transactionId, String txHash) {
        if (txHash == null || txHash.isBlank()) {
            throw new IllegalArgumentException("Transaction hash is required");
        }

        int updatedRows = walletTransactionRepository.updateTxHashAndStatusById(
                transactionId,
                txHash,
                TransactionStatus.CONFIRMED
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
    }
}