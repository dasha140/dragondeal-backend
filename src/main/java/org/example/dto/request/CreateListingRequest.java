package org.example.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.DeliveryType;

import java.math.BigDecimal;
import java.util.List;

public record CreateListingRequest(

        @NotNull(message = "Seller id is required")
        Long sellerId,

        @NotNull(message = "Category id is required")
        Long categoryId,

        @NotNull(message = "Game id is required")
        Long gameId,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        @Size(max = 20, message = "Currency must be at most 20 characters")
        String currency,

        @NotNull(message = "Network is required")
        BlockchainNetwork network,

        @NotNull(message = "Delivery type is required")
        DeliveryType deliveryType,

        List<String> imageUrls
) {
}