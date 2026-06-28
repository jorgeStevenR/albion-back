package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.PenaltyDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManualPenaltyRequest {

    @NotNull
    private Long playerId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private PenaltyDirection direction;

    @NotBlank
    private String reason;
}
