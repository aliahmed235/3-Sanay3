package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.BehaviorEventType;
import com.sany3.graduation_project.entites.ServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackEventRequest {

    @NotNull(message = "Event type is required")
    private BehaviorEventType eventType;

    private ServiceType serviceType;

    private LocalDateTime occurredAt;

    private String sessionId;

    private String clientTimezone;
}
