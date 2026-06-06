package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationStatsResponse {

    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
    private long totalCount;
}
