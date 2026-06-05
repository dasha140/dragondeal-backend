package org.example.dto.request;

public record CreateDisputeEvidenceByCurrentUserRequest(
        String text,
        String imageUrl
) {
}