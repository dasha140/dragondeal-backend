package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDisputeEvidenceRequest(

        @NotNull(message = "User id is required")
        Long userId,

        @Size(max = 5000, message = "Text must be at most 5000 characters")
        String text,

        @Size(max = 500, message = "Image url must be at most 500 characters")
        String imageUrl
) {
}