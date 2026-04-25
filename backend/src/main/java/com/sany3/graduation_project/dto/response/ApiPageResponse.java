package com.sany3.graduation_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API Response wrapper for paginated responses
 * Used when returning Page<T> from repositories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiPageResponse<T> {

    private boolean success;
    private String message;
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private LocalDateTime timestamp;

    /**
     * Create from Spring Page object
     */
    public static <T> ApiPageResponse<T> of(Page<T> page, String message) {
        return ApiPageResponse.<T>builder()
                .success(true)
                .message(message)
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create from Spring Page object with default message
     */
    public static <T> ApiPageResponse<T> of(Page<T> page) {
        return of(page, "Data retrieved successfully");
    }

    /**
     * Create error page response
     */
    public static <T> ApiPageResponse<T> error(String message) {
        return ApiPageResponse.<T>builder()
                .success(false)
                .message(message)
                .content(List.of())
                .timestamp(LocalDateTime.now())
                .build();
    }
}