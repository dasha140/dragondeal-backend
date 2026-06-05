package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportTicketRequest(
        @NotBlank(message = "Subject is required")
        @Size(min = 3, max = 200, message = "Subject must be between 3 and 200 characters")
        String subject,

        @NotBlank(message = "Message is required")
        @Size(min = 10, message = "Message must be at least 10 characters")
        String message
) {
}
