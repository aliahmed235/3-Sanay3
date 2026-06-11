package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.SupportCategory;
import com.sany3.graduation_project.entites.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long requestId;
    private String requestTitle;
    private SupportCategory category;
    private String categoryDisplayName;
    private String description;
    private TicketStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String message;
}
