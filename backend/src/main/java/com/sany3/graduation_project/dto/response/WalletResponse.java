package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal balance;
    private String balanceStatus;
    private LocalDateTime createdAt;
}
