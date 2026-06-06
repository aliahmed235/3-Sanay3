package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.UserBehaviorEventRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.TrackEventRequest;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.entites.UserBehaviorEvent;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BehaviorEventService {

    private final UserBehaviorEventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Track a user behavior event
     *
     * @param userId  Authenticated user ID
     * @param request Event details from the client
     * @return Saved event
     */
    public UserBehaviorEvent trackEvent(Long userId, TrackEventRequest request) {
        log.debug("Tracking event {} for user {}", request.getEventType(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime occurredAt = request.getOccurredAt() != null
                ? request.getOccurredAt()
                : LocalDateTime.now();

        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .user(user)
                .eventType(request.getEventType())
                .serviceType(request.getServiceType())
                .sessionId(request.getSessionId())
                .clientTimezone(request.getClientTimezone())
                .occurredAt(occurredAt)
                .build();

        event = eventRepository.save(event);
        log.info("Event tracked: {} for user {} (id={})", request.getEventType(), userId, event.getId());

        return event;
    }
}
