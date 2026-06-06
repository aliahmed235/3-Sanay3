package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.BehaviorEventService;
import com.sany3.graduation_project.dto.request.TrackEventRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.TrackEventResponse;
import com.sany3.graduation_project.entites.UserBehaviorEvent;
import com.sany3.graduation_project.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EventTrackingController {

    private final BehaviorEventService behaviorEventService;

    /**
     * POST /api/events — Track a user behavior event
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TrackEventResponse>> trackEvent(
            @Valid @RequestBody TrackEventRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        UserBehaviorEvent event = behaviorEventService.trackEvent(userId, request);

        TrackEventResponse response = TrackEventResponse.builder()
                .eventId(event.getId())
                .eventType(event.getEventType().name())
                .occurredAt(event.getOccurredAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.EVENT_TRACKED));
    }
}
