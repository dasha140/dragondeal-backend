package org.example.repository;

import org.example.domain.DisputeEvidence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "dispute",
            "user"
    })
    List<DisputeEvidence> findAll();

    @EntityGraph(attributePaths = {
            "dispute",
            "user"
    })
    List<DisputeEvidence> findByDispute_Id(Long disputeId);

    @EntityGraph(attributePaths = {
            "dispute",
            "user"
    })
    List<DisputeEvidence> findByUser_Id(Long userId);
}