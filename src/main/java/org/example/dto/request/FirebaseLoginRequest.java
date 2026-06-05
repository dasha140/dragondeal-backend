package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FirebaseLoginRequest(
        @NotBlank
        String idToken
) {
}