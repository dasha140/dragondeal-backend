package org.example.dto.response;

import org.example.domain.enums.TicketStatus;

import java.time.LocalDateTime;

public record SupportTicketResponse(
        Long id,
        String subject,
        String message,
        String adminReply,
        TicketStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime repliedAt
) {
}
