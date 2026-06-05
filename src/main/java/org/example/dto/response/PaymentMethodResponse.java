package org.example.dto.response;

import org.example.domain.enums.CryptoCurrency;
import org.example.domain.enums.CryptoNetwork;

import java.math.BigDecimal;

public record PaymentMethodResponse(
        Long id,
        CryptoCurrency currency,
        CryptoNetwork network,
        Long chainId,
        String tokenContract,
        String escrowContract,
        Integer decimals,
        BigDecimal minAmount,
        Boolean enabled
) {
}