package com.sany3.graduation_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkSummaryResponse {

    private Long requestId;
    private String requestTitle;
    private String description;
    private List<String> beforePhotos;
    private List<String> afterPhotos;
    private String customerName;
    private String serviceType;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
