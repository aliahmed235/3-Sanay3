package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.Services.SupportTicketService;
import com.sany3.graduation_project.dto.request.CreateSupportTicketRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.SupportChatStepResponse;
import com.sany3.graduation_project.dto.response.SupportChatStepResponse.Choice;
import com.sany3.graduation_project.dto.response.SupportTicketResponse;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.SupportCategory;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
public class SupportController {

    private final SupportTicketService supportTicketService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    // ══════════════════════════════════════════════════════════
    //  CHAT FLOW ENDPOINTS (step-by-step conversation)
    // ══════════════════════════════════════════════════════════

    /**
     * Step 1: Start the support chat
     * GET /api/support/chat/start
     *
     * Returns: "How can we help you today?" + category buttons
     */
    @GetMapping("/chat/start")
    public ResponseEntity<ApiResponse<SupportChatStepResponse>> startChat(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String role = user.hasRole("SERVICE_PROVIDER") ? "SERVICE_PROVIDER" : "USER";

        List<Choice> categories = Arrays.stream(SupportCategory.values())
                .filter(c -> c.isAllowedFor(role))
                .map(c -> Choice.builder()
                        .value(c.name())
                        .label(c.getDisplayName())
                        .build())
                .toList();

        SupportChatStepResponse step = SupportChatStepResponse.builder()
                .step("SELECT_CATEGORY")
                .botMessage("How can we help you today?")
                .inputType("CHOICE")
                .choices(categories)
                .ticketCreated(false)
                .build();

        return ResponseEntity.ok(ApiResponse.success(step, "Support chat started"));
    }

    /**
     * Step 2: User picks a category → show their requests to choose from
     * POST /api/support/chat/pick-category
     * Body: { "category": "LATE_PROVIDER" }
     *
     * Returns: "Which request do you want to report?" + request buttons
     */
    @PostMapping("/chat/pick-category")
    public ResponseEntity<ApiResponse<SupportChatStepResponse>> pickCategory(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        String category = body.get("category");
        log.info("Support chat: user {} picked category {}", userId, category);

        List<ServiceRequest> userRequests = serviceRequestRepository
                .findByCustomerIdOrderByIdDesc(userId, PageRequest.of(0, 10))
                .getContent();

        List<Choice> choices = userRequests.stream()
                .map(r -> Choice.builder()
                        .value(String.valueOf(r.getId()))
                        .label(r.getTitle() + " - " + r.getServiceType().name())
                        .build())
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        choices.add(Choice.builder()
                .value("NONE")
                .label("General complaint (no specific request)")
                .build());

        SupportChatStepResponse step = SupportChatStepResponse.builder()
                .step("SELECT_REQUEST")
                .botMessage("Which request would you like to report?")
                .inputType("CHOICE")
                .choices(choices)
                .ticketCreated(false)
                .build();

        return ResponseEntity.ok(ApiResponse.success(step, "Category selected"));
    }

    /**
     * Step 3: User picks a request → ask for description
     * POST /api/support/chat/pick-request
     * Body: { "requestId": "15" }  (or "NONE" for general)
     *
     * Returns: "Please describe what happened:" + text input
     */
    @PostMapping("/chat/pick-request")
    public ResponseEntity<ApiResponse<SupportChatStepResponse>> pickRequest(
            @RequestBody Map<String, String> body) {

        String requestId = body.get("requestId");
        log.info("Support chat: user picked request {}", requestId);

        SupportChatStepResponse step = SupportChatStepResponse.builder()
                .step("DESCRIBE")
                .botMessage("Please describe what happened:")
                .inputType("TEXT")
                .choices(null)
                .ticketCreated(false)
                .build();

        return ResponseEntity.ok(ApiResponse.success(step, "Request selected"));
    }

    /**
     * Step 4: User submits description → ticket created
     * POST /api/support/chat/submit
     * Body: { "category": "LATE_PROVIDER", "requestId": "15", "description": "..." }
     *
     * Returns: "Your ticket has been submitted. We will contact you within 24 hours."
     */
    @PostMapping("/chat/submit")
    public ResponseEntity<ApiResponse<SupportChatStepResponse>> submitTicket(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        String category = body.get("category");
        String requestIdStr = body.get("requestId");
        String description = body.get("description");

        log.info("Support chat submit: user {}, category {}, request {}", userId, category, requestIdStr);

        Long requestId = null;
        if (requestIdStr != null && !requestIdStr.equals("NONE") && !requestIdStr.isBlank()) {
            requestId = Long.parseLong(requestIdStr);
        }

        CreateSupportTicketRequest ticketRequest = CreateSupportTicketRequest.builder()
                .category(SupportCategory.valueOf(category))
                .requestId(requestId)
                .description(description)
                .build();

        SupportTicketResponse ticket = supportTicketService.createTicket(userId, ticketRequest);

        SupportChatStepResponse step = SupportChatStepResponse.builder()
                .step("DONE")
                .botMessage("Your ticket has been submitted. We will contact you within 24 hours.")
                .inputType("NONE")
                .choices(null)
                .ticketCreated(true)
                .ticketId(ticket.getId())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(step, "Your ticket has been submitted. We will contact you within 24 hours."));
    }

    // ══════════════════════════════════════════════════════════
    //  TICKET HISTORY (user can check their past tickets)
    // ══════════════════════════════════════════════════════════

    /**
     * Get my support tickets
     * GET /api/support/tickets/my
     */
    @GetMapping("/tickets/my")
    public ResponseEntity<ApiResponse<Page<SupportTicketResponse>>> getMyTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = (Long) authentication.getPrincipal();
        Page<SupportTicketResponse> tickets = supportTicketService.getMyTickets(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(tickets, "Tickets retrieved"));
    }
}
