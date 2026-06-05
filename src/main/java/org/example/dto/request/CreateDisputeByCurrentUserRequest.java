package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDisputeByCurrentUserRequest(
        @NotNull(message = "Order id is required")
        Long orderId,

        @NotBlank(message = "Reason is required")
        String reason
) {
}