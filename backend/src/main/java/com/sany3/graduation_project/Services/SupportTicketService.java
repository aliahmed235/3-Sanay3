package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.Repositories.SupportTicketRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.CreateSupportTicketRequest;
import com.sany3.graduation_project.dto.response.SupportTicketResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public SupportTicketResponse createTicket(Long userId, CreateSupportTicketRequest request) {
        log.info("Creating support ticket for user: {}, category: {}", userId, request.getCategory());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ServiceRequest serviceRequest = null;
        if (request.getRequestId() != null) {
            serviceRequest = serviceRequestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        }

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .request(serviceRequest)
                .category(request.getCategory())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .build();

        ticket = supportTicketRepository.save(ticket);
        log.info("Support ticket created with ID: {}", ticket.getId());

        SupportTicketResponse response = toResponse(ticket);
        response.setMessage("Your ticket has been submitted. We will contact you within 24 hours.");
        return response;
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getMyTickets(Long userId, Pageable pageable) {
        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getAllTickets(Pageable pageable) {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return supportTicketRepository.findByStatusOrderByCreatedAtAsc(status, pageable)
                .map(this::toResponse);
    }

    public SupportTicketResponse resolveTicket(Long ticketId, String adminNote) {
        log.info("Resolving support ticket: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setAdminNote(adminNote);
        ticket.setResolvedAt(LocalDateTime.now());
        ticket = supportTicketRepository.save(ticket);

        log.info("Support ticket {} resolved", ticketId);
        return toResponse(ticket);
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUser().getId())
                .userName(ticket.getUser().getName())
                .requestId(ticket.getRequest() != null ? ticket.getRequest().getId() : null)
                .requestTitle(ticket.getRequest() != null ? ticket.getRequest().getTitle() : null)
                .category(ticket.getCategory())
                .categoryDisplayName(ticket.getCategory().getDisplayName())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .adminNote(ticket.getAdminNote())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
}
