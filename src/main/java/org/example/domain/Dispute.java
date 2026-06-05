package org.example.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.domain.enums.DisputeStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dispute")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private AppOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opened_by", nullable = false)
    private AppUser openedBy;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DisputeStatus status;

    @Column(name = "admin_decision")
    private String adminDecision;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dispute")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<DisputeEvidence> evidenceList = new java.util.ArrayList<>();
}