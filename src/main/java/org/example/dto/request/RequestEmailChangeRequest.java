package org.example.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestEmailChangeRequest(

        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String newEmail,

        @NotBlank(message = "Current password is required")
        String currentPassword
) {
}