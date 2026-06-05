package org.example.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.DeliveryType;

import java.math.BigDecimal;
import java.util.List;

public record CreateListingByCurrentUserRequest(
        @NotNull(message = "Category id is required")
        Long categoryId,

        @NotNull(message = "Game id is required")
        Long gameId,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Network is required")
        BlockchainNetwork network,

        @NotNull(message = "Delivery type is required")
        DeliveryType deliveryType,

        List<String> imageUrls
) {
}