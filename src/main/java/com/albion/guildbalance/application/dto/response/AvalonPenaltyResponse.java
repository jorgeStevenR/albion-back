package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.PenaltyDirection;
import com.albion.guildbalance.domain.enums.PenaltyStatus;
import com.albion.guildbalance.domain.enums.PenaltyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AvalonPenaltyResponse {

    private Long id;
    private Long avalonId;
    private String avalonZone;
    private Long playerId;
    private String playerName;
    private BigDecimal amount;
    private PenaltyDirection direction;
    private PenaltyType type;
    private String reason;
    private PenaltyStatus status;
    private Long createdById;
    private String createdByName;
    private Long relatedPlayerId;
    private String relatedPlayerName;
    private LocalDateTime createdAt;
    private boolean hasAppeal;
    private String appealStatus;
}
