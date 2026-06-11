package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportChatStepResponse {

    private String step;
    private String botMessage;
    private String inputType;
    private List<Choice> choices;

    private Boolean ticketCreated;
    private Long ticketId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Choice {
        private String value;
        private String label;
    }
}
