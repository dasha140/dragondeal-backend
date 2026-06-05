package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.SupportTicket;
import org.example.domain.enums.TicketStatus;
import org.example.domain.enums.UserRole;
import org.example.dto.request.AdminReplySupportTicketRequest;
import org.example.dto.request.CreateSupportTicketRequest;
import org.example.dto.request.UpdateSupportTicketStatusRequest;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional
    public SupportTicket createForUser(AppUser currentUser, CreateSupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .user(currentUser)
                .subject(request.subject())
                .message(request.message())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return supportTicketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findMyTickets(AppUser currentUser) {
        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public SupportTicket findById(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findAllForAdmin(AppUser currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view all support tickets");
        }

        return supportTicketRepository.findAll();
    }

    @Transactional
    public void replyToTicket(Long id, AppUser admin, AdminReplySupportTicketRequest request) {
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can reply to support tickets");
        }

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + id));

        ticket.setAdminReply(request.reply());
        ticket.setStatus(TicketStatus.ANSWERED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setRepliedAt(LocalDateTime.now());

        supportTicketRepository.save(ticket);
    }

    @Transactional
    public void updateStatus(Long id, AppUser admin, UpdateSupportTicketStatusRequest request) {
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can change support ticket status");
        }

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + id));

        ticket.setStatus(request.status());
        ticket.setUpdatedAt(LocalDateTime.now());

        supportTicketRepository.save(ticket);
    }
}
