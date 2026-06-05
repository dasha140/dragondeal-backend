package org.example.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        Long id,
        Long orderId,
        Long buyerId,
        String buyerUsername,
        Long sellerId,
        String sellerUsername,
        LocalDateTime createdAt,
        List<ChatMessageResponse> messages
) {
}