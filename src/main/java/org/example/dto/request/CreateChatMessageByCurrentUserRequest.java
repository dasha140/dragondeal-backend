package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateChatMessageByCurrentUserRequest(
        @NotNull(message = "Chat id is required")
        Long chatId,

        String content,

        String imageUrl
) {
}