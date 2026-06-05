package org.example.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterConfirmRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "Code must contain 6 digits")
        String code
) {
}