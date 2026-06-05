package org.example.mapper;

import org.example.domain.WalletTransaction;
import org.example.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(WalletTransaction transaction) {
        Long orderId = transaction.getOrder() == null
                ? null
                : transaction.getOrder().getId();

        return new TransactionResponse(
                transaction.getId(),
                orderId,
                transaction.getUser().getId(),
                transaction.getUser().getUsername(),
                transaction.getTxHash(),
                transaction.getFromWallet(),
                transaction.getToWallet(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getNetwork(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

    public List<TransactionResponse> toResponseList(List<WalletTransaction> transactions) {
        return transactions.stream()
                .map(this::toResponse)
                .toList();
    }
}