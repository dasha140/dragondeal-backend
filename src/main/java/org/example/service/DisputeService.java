package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.AppUser;
import org.example.domain.Dispute;
import org.example.domain.DisputeEvidence;
import org.example.domain.enums.DisputeStatus;
import org.example.domain.enums.OrderStatus;
import org.example.domain.enums.UserRole;
import org.example.dto.request.CreateDisputeByCurrentUserRequest;
import org.example.dto.request.CreateDisputeEvidenceByCurrentUserRequest;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppOrderRepository;
import org.example.repository.DisputeEvidenceRepository;
import org.example.repository.DisputeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeEvidenceRepository disputeEvidenceRepository;
    private final AppOrderRepository appOrderRepository;

    @Transactional(readOnly = true)
    public List<Dispute> findAllDisputesForAdmin(AppUser currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view all disputes");
        }

        return disputeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Dispute findDisputeWithDetailsForCurrentUser(AppUser currentUser, Long disputeId) {
        Dispute dispute = disputeRepository.findWithDetailsById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        if (!canViewDispute(currentUser, dispute)) {
            throw new IllegalArgumentException("Only dispute order participants can view this dispute");
        }

        return dispute;
    }

    @Transactional(readOnly = true)
    public List<Dispute> findDisputesByOrderForCurrentUser(AppUser currentUser, Long orderId) {
        AppOrder order = appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isOrderParticipantOrAdmin(currentUser, order)) {
            throw new IllegalArgumentException("Only order participants can view order disputes");
        }

        return disputeRepository.findByOrder_Id(orderId);
    }

    @Transactional(readOnly = true)
    public List<Dispute> findMyDisputes(AppUser currentUser) {
        return disputeRepository.findByOpenedBy_Id(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<Dispute> findOpenDisputesForAdmin(AppUser currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view open disputes");
        }

        return disputeRepository.findByStatus(DisputeStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<Dispute> findResolvedDisputesForAdmin(AppUser currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view resolved disputes");
        }

        return disputeRepository.findByStatus(DisputeStatus.RESOLVED);
    }

    @Transactional(readOnly = true)
    public List<DisputeEvidence> findEvidenceByDisputeForCurrentUser(AppUser currentUser, Long disputeId) {
        Dispute dispute = disputeRepository.findWithDetailsById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        if (!canViewDispute(currentUser, dispute)) {
            throw new IllegalArgumentException("Only dispute order participants can view evidence");
        }

        return disputeEvidenceRepository.findByDispute_Id(disputeId);
    }

    @Transactional(readOnly = true)
    public List<DisputeEvidence> findMyEvidence(AppUser currentUser) {
        return disputeEvidenceRepository.findByUser_Id(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<DisputeEvidence> findEvidenceByUserForAdmin(AppUser currentUser, Long userId) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view user evidence");
        }

        return disputeEvidenceRepository.findByUser_Id(userId);
    }

    @Transactional
    public Dispute createDisputeForCurrentUser(AppUser currentUser, CreateDisputeByCurrentUserRequest request) {
        AppOrder order = appOrderRepository.findWithDetailsById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        if (!isOrderParticipantOrAdmin(currentUser, order)) {
            throw new IllegalArgumentException("User is not a participant of this order");
        }

        if (disputeRepository.existsByOrder_Id(order.getId())) {
            throw new IllegalArgumentException("Dispute already exists for this order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot open dispute for completed order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot open dispute for cancelled order");
        }

        if (order.getStatus() == OrderStatus.DISPUTED) {
            throw new IllegalArgumentException("Order is already disputed");
        }

        Dispute dispute = Dispute.builder()
                .order(order)
                .openedBy(currentUser)
                .reason(request.reason())
                .status(DisputeStatus.OPEN)
                .adminDecision("")
                .createdAt(LocalDateTime.now())
                .resolvedAt(null)
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);

        order.setStatus(OrderStatus.DISPUTED);
        appOrderRepository.save(order);

        return savedDispute;
    }

    @Transactional
    public DisputeEvidence addEvidenceForCurrentUser(
            AppUser currentUser,
            Long disputeId,
            CreateDisputeEvidenceByCurrentUserRequest request
    ) {
        Dispute dispute = disputeRepository.findWithDetailsById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        if (!canViewDispute(currentUser, dispute)) {
            throw new IllegalArgumentException("User is not a participant of this dispute order");
        }

        boolean textBlank = request.text() == null || request.text().isBlank();
        boolean imageBlank = request.imageUrl() == null || request.imageUrl().isBlank();

        if (textBlank && imageBlank) {
            throw new IllegalArgumentException("Evidence must contain text or image");
        }

        DisputeEvidence evidence = DisputeEvidence.builder()
                .dispute(dispute)
                .user(currentUser)
                .text(textBlank ? "" : request.text())
                .imageUrl(imageBlank ? "" : request.imageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return disputeEvidenceRepository.save(evidence);
    }

    @Transactional
    public void resolveDisputeForAdmin(AppUser currentUser, Long disputeId, String adminDecision) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can resolve disputes");
        }

        if (adminDecision == null || adminDecision.isBlank()) {
            throw new IllegalArgumentException("Admin decision is required");
        }

        Dispute dispute = disputeRepository.findWithDetailsById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        int updatedRows = disputeRepository.resolveDispute(
                disputeId,
                DisputeStatus.RESOLVED,
                adminDecision,
                LocalDateTime.now()
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Dispute not found: " + disputeId);
        }

        AppOrder order = dispute.getOrder();

        if (order != null && order.getStatus() == OrderStatus.DISPUTED) {
            order.setStatus(OrderStatus.COMPLETED);
            appOrderRepository.save(order);
        }
    }

    @Transactional
    public void rejectDisputeForAdmin(AppUser currentUser, Long disputeId, String adminDecision) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can reject disputes");
        }

        if (adminDecision == null || adminDecision.isBlank()) {
            throw new IllegalArgumentException("Admin decision is required");
        }

        Dispute dispute = disputeRepository.findWithDetailsById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        int updatedRows = disputeRepository.resolveDispute(
                disputeId,
                DisputeStatus.REJECTED,
                adminDecision,
                LocalDateTime.now()
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Dispute not found: " + disputeId);
        }

        AppOrder order = dispute.getOrder();

        if (order != null && order.getStatus() == OrderStatus.DISPUTED) {
            order.setStatus(OrderStatus.PAID);
            appOrderRepository.save(order);
        }
    }

    private boolean canViewDispute(AppUser currentUser, Dispute dispute) {
        return isOrderParticipantOrAdmin(currentUser, dispute.getOrder());
    }

    private boolean isOrderParticipantOrAdmin(AppUser currentUser, AppOrder order) {
        boolean isBuyer = order.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = order.getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        return isBuyer || isSeller || isAdmin;
    }
}