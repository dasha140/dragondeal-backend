package org.example.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(max = 80, message = "Display name must be less than 80 characters")
        String displayName,

        @Size(max = 500, message = "Avatar url must be less than 500 characters")
        String avatarUrl
) {
}