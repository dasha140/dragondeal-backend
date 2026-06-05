package org.example.repository;

import org.example.domain.Dispute;
import org.example.domain.enums.DisputeStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    boolean existsByOrder_Id(Long orderId);

    @Override
    @EntityGraph(attributePaths = {
            "order",
            "openedBy",
            "evidenceList",
            "evidenceList.user"
    })
    List<Dispute> findAll();

    @EntityGraph(attributePaths = {
            "order",
            "openedBy",
            "evidenceList",
            "evidenceList.user"
    })
    Optional<Dispute> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "order",
            "openedBy",
            "evidenceList",
            "evidenceList.user"
    })
    List<Dispute> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {
            "order",
            "openedBy",
            "evidenceList",
            "evidenceList.user"
    })
    List<Dispute> findByOpenedBy_Id(Long openedById);

    @EntityGraph(attributePaths = {
            "order",
            "openedBy",
            "evidenceList",
            "evidenceList.user"
    })
    List<Dispute> findByStatus(DisputeStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Dispute d
            set d.status = :status
            where d.id = :id
            """)
    int updateStatusById(@Param("id") Long id,
                         @Param("status") DisputeStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Dispute d
            set d.status = :status,
                d.adminDecision = :adminDecision,
                d.resolvedAt = :resolvedAt
            where d.id = :id
            """)
    int resolveDispute(@Param("id") Long id,
                       @Param("status") DisputeStatus status,
                       @Param("adminDecision") String adminDecision,
                       @Param("resolvedAt") LocalDateTime resolvedAt);
}