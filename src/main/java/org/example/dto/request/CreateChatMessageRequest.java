package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChatMessageRequest(

        @NotNull(message = "Chat id is required")
        Long chatId,

        @NotNull(message = "Sender id is required")
        Long senderId,

        @Size(max = 5000, message = "Message content must be at most 5000 characters")
        String content,

        @Size(max = 500, message = "Image url must be at most 500 characters")
        String imageUrl
) {
}