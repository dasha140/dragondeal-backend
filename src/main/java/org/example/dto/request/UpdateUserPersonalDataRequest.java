package org.example.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPersonalDataRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 80, message = "Display name must be at most 80 characters")
        String displayName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email
) {
}