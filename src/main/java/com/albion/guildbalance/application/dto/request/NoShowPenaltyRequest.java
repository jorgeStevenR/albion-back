package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NoShowPenaltyRequest {

    @NotNull
    private Long noShowPlayerId;

    @NotNull
    private Long replacementPlayerId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String reason;
}
