package org.example.mapper;

import org.example.domain.Dispute;
import org.example.domain.DisputeEvidence;
import org.example.dto.response.DisputeEvidenceResponse;
import org.example.dto.response.DisputeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DisputeMapper {

    public DisputeEvidenceResponse toEvidenceResponse(DisputeEvidence evidence) {
        return new DisputeEvidenceResponse(
                evidence.getId(),
                evidence.getDispute().getId(),
                evidence.getUser().getId(),
                evidence.getUser().getUsername(),
                evidence.getText(),
                evidence.getImageUrl(),
                evidence.getCreatedAt()
        );
    }

    public List<DisputeEvidenceResponse> toEvidenceResponseList(List<DisputeEvidence> evidenceList) {
        if (evidenceList == null) {
            return Collections.emptyList();
        }

        return evidenceList.stream()
                .map(this::toEvidenceResponse)
                .toList();
    }

    public DisputeResponse toResponse(Dispute dispute) {
        List<DisputeEvidenceResponse> evidenceList = dispute.getEvidenceList() == null
                ? Collections.emptyList()
                : dispute.getEvidenceList()
                .stream()
                .map(this::toEvidenceResponse)
                .toList();

        return new DisputeResponse(
                dispute.getId(),
                dispute.getOrder().getId(),
                dispute.getOpenedBy().getId(),
                dispute.getOpenedBy().getUsername(),
                dispute.getReason(),
                dispute.getStatus(),
                dispute.getAdminDecision(),
                dispute.getCreatedAt(),
                dispute.getResolvedAt(),
                evidenceList
        );
    }

    public List<DisputeResponse> toResponseList(List<Dispute> disputes) {
        return disputes.stream()
                .map(this::toResponse)
                .toList();
    }
}