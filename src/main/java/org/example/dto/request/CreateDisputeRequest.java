package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDisputeRequest(

        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "Opened by user id is required")
        Long openedById,

        @NotBlank(message = "Reason is required")
        @Size(max = 5000, message = "Reason must be at most 5000 characters")
        String reason
) {
}