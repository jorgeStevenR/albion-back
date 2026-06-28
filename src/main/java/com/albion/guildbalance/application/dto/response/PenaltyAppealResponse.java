package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.AppealStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PenaltyAppealResponse {

    private Long id;
    private Long penaltyId;
    private Long avalonId;
    private String avalonZone;
    private Long playerId;
    private String playerName;
    private BigDecimal amount;
    private String penaltyReason;
    private String reason;
    private AppealStatus status;
    private String reviewNotes;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
