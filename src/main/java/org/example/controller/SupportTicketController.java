package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.SupportTicket;
import org.example.dto.request.CreateSupportTicketRequest;
import org.example.dto.response.SupportTicketResponse;
import org.example.service.CurrentUserService;
import org.example.service.SupportTicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public SupportTicketResponse createTicket(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateSupportTicketRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        SupportTicket ticket = supportTicketService.createForUser(currentUser, request);
        return toResponse(ticket);
    }

    @GetMapping("/my")
    public List<SupportTicketResponse> findMyTickets(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<SupportTicket> tickets = supportTicketService.findMyTickets(currentUser);
        return tickets.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public SupportTicketResponse findTicketById(
            HttpServletRequest httpRequest,
            @PathVariable Long id
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        SupportTicket ticket = supportTicketService.findById(id);

        if (!ticket.getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != org.example.domain.enums.UserRole.ADMIN) {
            throw new IllegalArgumentException("You can only view your own support tickets");
        }

        return toResponse(ticket);
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
