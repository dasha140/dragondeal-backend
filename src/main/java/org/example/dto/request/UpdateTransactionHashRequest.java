package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTransactionHashRequest(

        @NotBlank(message = "Transaction hash is required")
        @Size(max = 255, message = "Transaction hash must be at most 255 characters")
        String txHash
) {
}