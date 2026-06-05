package org.example.dto.blockchain;

import java.math.BigInteger;

public record EscrowDealBlockchainData(
        BigInteger id,
        String buyer,
        String seller,
        String token,
        BigInteger amount,
        BigInteger status,
        BigInteger createdAt,
        BigInteger paidAt,
        BigInteger completedAt
) {
}