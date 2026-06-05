package org.example.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long orderId,
        Long reviewerId,
        String reviewerUsername,
        Long targetUserId,
        String targetUsername,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}