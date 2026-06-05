package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOrderByCurrentUserRequest(
        @NotNull(message = "Listing id is required")
        Long listingId
) {
}