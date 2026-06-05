package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentIntentRequest(
        @NotNull Long orderId,
        @NotNull Long paymentMethodId
) {
}