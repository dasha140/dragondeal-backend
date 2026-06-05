package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMessageImageRequest(

        @NotBlank(message = "Image url is required")
        @Size(max = 500, message = "Image url must be at most 500 characters")
        String imageUrl
) {
}