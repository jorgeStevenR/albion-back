package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMoneyRequestDto {

    @NotNull
    @DecimalMin(value = "1")
    private BigDecimal amount;

    @NotBlank
    private String reason;
}
