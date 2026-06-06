package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NudgeJobResultResponse {

    private int eligibleUsers;
    private int notificationsSent;
    private int notificationsFailed;
    private LocalDateTime triggeredAt;
}
