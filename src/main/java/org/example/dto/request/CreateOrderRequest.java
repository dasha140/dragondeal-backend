package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

        @NotNull(message = "Buyer id is required")
        Long buyerId,

        @NotNull(message = "Listing id is required")
        Long listingId
) {
}