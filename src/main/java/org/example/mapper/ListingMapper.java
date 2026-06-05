package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.Listing;
import org.example.domain.ListingImage;
import org.example.domain.enums.OrderStatus;
import org.example.dto.response.ListingResponse;
import org.example.repository.AppOrderRepository;
import org.example.repository.ReviewRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListingMapper {

    private final AppOrderRepository appOrderRepository;
    private final ReviewRepository reviewRepository;

    public ListingResponse toResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        Long sellerId = listing.getSeller() != null
                ? listing.getSeller().getId()
                : null;

        Long sellerCompletedOrdersCount = 0L;
        BigDecimal sellerRating = BigDecimal.ZERO;

        if (sellerId != null) {
            sellerCompletedOrdersCount = appOrderRepository.countBySeller_IdAndStatus(
                    sellerId,
                    OrderStatus.COMPLETED
            );

            Double averageRating = reviewRepository.getAverageRatingByTargetUserId(sellerId);

            if (averageRating == null) {
                averageRating = 0.0;
            }

            sellerRating = BigDecimal.valueOf(averageRating)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new ListingResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCurrency(),
                listing.getNetwork() != null ? listing.getNetwork().name() : "",
                listing.getDeliveryType(),
                listing.getStatus(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),

                sellerId,
                listing.getSeller() != null ? listing.getSeller().getUsername() : "",
                listing.getSeller() != null ? listing.getSeller().getAvatarUrl() : "",
                sellerRating,
                sellerCompletedOrdersCount,

                listing.getCategory() != null ? listing.getCategory().getId() : null,
                listing.getCategory() != null ? listing.getCategory().getName() : "",

                listing.getGame() != null ? listing.getGame().getId() : null,
                listing.getGame() != null ? listing.getGame().getName() : "",
                listing.getGame() != null ? listing.getGame().getImageUrl() : "",

                toImageUrls(listing)
        );
    }

    public List<ListingResponse> toResponseList(List<Listing> listings) {
        return listings.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<String> toImageUrls(Listing listing) {
        if (listing.getImageList() == null) {
            return List.of();
        }

        return listing.getImageList()
                .stream()
                .map(ListingImage::getImageUrl)
                .toList();
    }
}