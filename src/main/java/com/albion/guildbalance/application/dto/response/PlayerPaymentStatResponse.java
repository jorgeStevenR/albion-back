package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlayerPaymentStatResponse {

    private Long playerId;
    private String playerName;
    private BigDecimal totalPaid;
}
