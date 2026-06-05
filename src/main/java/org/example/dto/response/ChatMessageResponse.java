package org.example.dto.response;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long chatId,
        Long senderId,
        String senderUsername,
        String content,
        String imageUrl,
        LocalDateTime createdAt
) {
}