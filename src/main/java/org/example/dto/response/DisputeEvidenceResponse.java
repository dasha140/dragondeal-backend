package org.example.dto.response;

import java.time.LocalDateTime;

public record DisputeEvidenceResponse(
        Long id,
        Long disputeId,
        Long userId,
        String username,
        String text,
        String imageUrl,
        LocalDateTime createdAt
) {
}