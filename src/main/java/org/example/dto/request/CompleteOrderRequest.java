package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteOrderRequest(

        @NotBlank(message = "Release transaction hash is required")
        @Size(max = 255, message = "Release transaction hash must be at most 255 characters")
        String releaseTxHash
) {
}