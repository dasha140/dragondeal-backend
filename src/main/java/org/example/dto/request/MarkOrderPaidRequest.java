package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarkOrderPaidRequest(

        @NotBlank(message = "Payment transaction hash is required")
        @Size(max = 255, message = "Payment transaction hash must be at most 255 characters")
        String paymentTxHash
) {
}