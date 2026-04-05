package com.sany3.graduation_project.dto.response;

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
public class SuccessResponse {
    private String message;
    private Object data;
    private Boolean success;
    private LocalDateTime timestamp;

    public SuccessResponse(String message, Object data) {
        this.message = message;
        this.data = data;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
}