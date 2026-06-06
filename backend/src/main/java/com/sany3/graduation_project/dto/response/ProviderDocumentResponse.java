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
public class ProviderDocumentResponse {

    private Long id;
    private String documentType;
    private String documentName;
    private String documentUrl;
    private Boolean isVerified;
    private LocalDateTime uploadedAt;
}
