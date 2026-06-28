package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionResponse {

    private Long id;
    private Long avalonId;
    private String avalonZone;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
