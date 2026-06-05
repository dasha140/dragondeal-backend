package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.TicketStatus;

public record UpdateSupportTicketStatusRequest(
        @NotNull(message = "Status is required")
        TicketStatus status
) {
}
