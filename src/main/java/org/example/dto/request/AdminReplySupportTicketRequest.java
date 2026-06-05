package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminReplySupportTicketRequest(
        @NotBlank(message = "Reply message is required")
        String reply
) {
}
