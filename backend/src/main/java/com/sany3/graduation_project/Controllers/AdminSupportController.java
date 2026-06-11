package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.SupportTicketService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.SupportTicketResponse;
import com.sany3.graduation_project.entites.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/support")
@RequiredArgsConstructor
@Slf4j
public class AdminSupportController {

    private final SupportTicketService supportTicketService;

    /**
     * Get all support tickets (admin)
     * GET /admin/support/tickets?page=0&size=10
     * GET /admin/support/tickets?status=OPEN&page=0&size=10
     */
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketResponse>>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /admin/support/tickets - status: {}", status);

        Page<SupportTicketResponse> tickets = status != null
                ? supportTicketService.getTicketsByStatus(status, PageRequest.of(page, size))
                : supportTicketService.getAllTickets(PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(tickets, "Tickets retrieved"));
    }

    /**
     * Resolve a support ticket (admin)
     * PUT /admin/support/tickets/{ticketId}/resolve
     */
    @PutMapping("/tickets/{ticketId}/resolve")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> resolveTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> body) {

        log.info("PUT /admin/support/tickets/{}/resolve", ticketId);

        String adminNote = body.getOrDefault("adminNote", "");
        SupportTicketResponse response = supportTicketService.resolveTicket(ticketId, adminNote);

        return ResponseEntity.ok(ApiResponse.success(response, "Ticket resolved"));
    }
}
