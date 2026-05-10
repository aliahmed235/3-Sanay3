package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.RequestStatus;
import com.sany3.graduation_project.entites.ServiceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleDayResponse {

    private Long requestId;
    private String title;
    private String description;
    private String address;
    private ServiceType serviceType;
    private RequestStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime acceptedAt;
    private String customerName;
    private String customerPhone;
    private String customerAvatar;
    private Integer estimatedTimeMinutes;
}
