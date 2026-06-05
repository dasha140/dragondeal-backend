package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveDisputeRequest(

        @NotBlank(message = "Admin decision is required")
        @Size(max = 2000, message = "Admin decision must be at most 2000 characters")
        String adminDecision
) {
}