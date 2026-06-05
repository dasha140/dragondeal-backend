package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMessageContentRequest(

        @NotBlank(message = "Message content is required")
        @Size(max = 5000, message = "Message content must be at most 5000 characters")
        String content
) {
}