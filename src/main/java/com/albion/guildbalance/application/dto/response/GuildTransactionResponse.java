package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GuildTransactionResponse {

    private Long id;
    private String type;
    private BigDecimal amount;
    private String description;
    private Long avalonId;
    private String avalonZone;
    private String playerName;
    private LocalDateTime createdAt;
}
