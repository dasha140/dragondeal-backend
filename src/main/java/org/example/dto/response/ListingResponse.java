package org.example.dto.response;

import org.example.domain.Listing;
import org.example.domain.enums.DeliveryType;
import org.example.domain.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;import org.example.domain.Listing;
import org.example.domain.enums.ListingStatus;

import java.util.List;

public record ListingResponse(

        Long id,
        String title,
        String description,
        BigDecimal price,
        String currency,
        String network,
        DeliveryType deliveryType,
        ListingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long sellerId,
        String sellerUsername,
        String sellerAvatarUrl,
        BigDecimal sellerRating,
        Long sellerCompletedOrdersCount,

        Long categoryId,
        String categoryName,

        Long gameId,
        String gameName,
        String gameImageUrl,

        List<String> imageUrls
) {

}


