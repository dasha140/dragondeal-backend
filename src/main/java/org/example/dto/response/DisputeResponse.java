package org.example.dto.response;

import org.example.domain.enums.DisputeStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DisputeResponse(
        Long id,
        Long orderId,
        Long openedById,
        String openedByUsername,
        String reason,
        DisputeStatus status,
        String adminDecision,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        List<DisputeEvidenceResponse> evidenceList
) {
}