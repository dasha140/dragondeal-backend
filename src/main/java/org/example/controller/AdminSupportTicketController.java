package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.SupportTicket;
import org.example.dto.request.AdminReplySupportTicketRequest;
import org.example.dto.request.UpdateSupportTicketStatusRequest;
import org.example.dto.response.SupportTicketResponse;
import org.example.service.CurrentUserService;
import org.example.service.SupportTicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<SupportTicketResponse> findAllTickets(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<SupportTicket> tickets = supportTicketService.findAllForAdmin(currentUser);
        return tickets.stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/reply")
    public void replyToTicket(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @Valid @RequestBody AdminReplySupportTicketRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        supportTicketService.replyToTicket(id, currentUser, request);
    }

    @PatchMapping("/{id}/status")
    public void updateTicketStatus(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportTicketStatusRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        supportTicketService.updateStatus(id, currentUser, request);
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getMessage(),
                ticket.getAdminReply(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getRepliedAt()
        );
    }
}
