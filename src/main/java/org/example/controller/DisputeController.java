package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Dispute;
import org.example.domain.DisputeEvidence;
import org.example.dto.request.CreateDisputeByCurrentUserRequest;
import org.example.dto.request.CreateDisputeEvidenceByCurrentUserRequest;
import org.example.dto.request.ResolveDisputeRequest;
import org.example.dto.response.DisputeEvidenceResponse;
import org.example.dto.response.DisputeResponse;
import org.example.mapper.DisputeMapper;
import org.example.service.CurrentUserService;
import org.example.service.DisputeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;
    private final DisputeMapper disputeMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<DisputeResponse> findAllDisputes(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Dispute> disputes = disputeService.findAllDisputesForAdmin(currentUser);
        return disputeMapper.toResponseList(disputes);
    }

    @GetMapping("/my")
    public List<DisputeResponse> findMyDisputes(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Dispute> disputes = disputeService.findMyDisputes(currentUser);
        return disputeMapper.toResponseList(disputes);
    }

    @GetMapping("/open")
    public List<DisputeResponse> findOpenDisputes(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Dispute> disputes = disputeService.findOpenDisputesForAdmin(currentUser);
        return disputeMapper.toResponseList(disputes);
    }

    @GetMapping("/resolved")
    public List<DisputeResponse> findResolvedDisputes(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Dispute> disputes = disputeService.findResolvedDisputesForAdmin(currentUser);
        return disputeMapper.toResponseList(disputes);
    }

    @GetMapping("/order/{orderId}")
    public List<DisputeResponse> findDisputesByOrder(
            HttpServletRequest httpRequest,
            @PathVariable Long orderId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Dispute> disputes = disputeService.findDisputesByOrderForCurrentUser(currentUser, orderId);
        return disputeMapper.toResponseList(disputes);
    }

    @GetMapping("/{disputeId}")
    public DisputeResponse findDisputeWithDetails(
            HttpServletRequest httpRequest,
            @PathVariable Long disputeId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Dispute dispute = disputeService.findDisputeWithDetailsForCurrentUser(currentUser, disputeId);
        return disputeMapper.toResponse(dispute);
    }

    @PostMapping
    public DisputeResponse createDispute(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateDisputeByCurrentUserRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Dispute dispute = disputeService.createDisputeForCurrentUser(currentUser, request);
        return disputeMapper.toResponse(dispute);
    }

    @GetMapping("/{disputeId}/evidence")
    public List<DisputeEvidenceResponse> findEvidenceByDispute(
            HttpServletRequest httpRequest,
            @PathVariable Long disputeId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<DisputeEvidence> evidenceList = disputeService.findEvidenceByDisputeForCurrentUser(currentUser, disputeId);
        return disputeMapper.toEvidenceResponseList(evidenceList);
    }

    @GetMapping("/evidence/my")
    public List<DisputeEvidenceResponse> findMyEvidence(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<DisputeEvidence> evidenceList = disputeService.findMyEvidence(currentUser);
        return disputeMapper.toEvidenceResponseList(evidenceList);
    }

    @GetMapping("/evidence/user/{userId}")
    public List<DisputeEvidenceResponse> findEvidenceByUser(
            HttpServletRequest httpRequest,
            @PathVariable Long userId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<DisputeEvidence> evidenceList = disputeService.findEvidenceByUserForAdmin(currentUser, userId);
        return disputeMapper.toEvidenceResponseList(evidenceList);
    }

    @PostMapping("/{disputeId}/evidence")
    public DisputeEvidenceResponse addEvidence(
            HttpServletRequest httpRequest,
            @PathVariable Long disputeId,
            @Valid @RequestBody CreateDisputeEvidenceByCurrentUserRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        DisputeEvidence evidence = disputeService.addEvidenceForCurrentUser(currentUser, disputeId, request);
        return disputeMapper.toEvidenceResponse(evidence);
    }

    @PatchMapping("/{disputeId}/resolve")
    public void resolveDispute(
            HttpServletRequest httpRequest,
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        disputeService.resolveDisputeForAdmin(currentUser, disputeId, request.adminDecision());
    }

    @PatchMapping("/{disputeId}/reject")
    public void rejectDispute(
            HttpServletRequest httpRequest,
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        disputeService.rejectDisputeForAdmin(currentUser, disputeId, request.adminDecision());
    }
}