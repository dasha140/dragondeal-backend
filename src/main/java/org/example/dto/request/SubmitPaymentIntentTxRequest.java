package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitPaymentIntentTxRequest(
        @NotBlank String escrowDealId,
        @NotBlank String paymentTxHash
) {
}