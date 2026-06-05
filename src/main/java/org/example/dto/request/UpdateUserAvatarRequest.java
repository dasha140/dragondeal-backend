package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserAvatarRequest(

        @NotBlank(message = "Avatar url is required")
        @Size(max = 500, message = "Avatar url must be at most 500 characters")
        String avatarUrl
) {
}