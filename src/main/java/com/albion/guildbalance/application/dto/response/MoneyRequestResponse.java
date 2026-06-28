package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.MoneyRequestStatus;
import com.albion.guildbalance.domain.enums.MoneyRequestType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MoneyRequestResponse {

    private Long id;
    private Long playerId;
    private String playerName;
    private MoneyRequestType type;
    private BigDecimal amount;
    private String reason;
    private MoneyRequestStatus status;
    private String reviewedByName;
    private String reviewNotes;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
