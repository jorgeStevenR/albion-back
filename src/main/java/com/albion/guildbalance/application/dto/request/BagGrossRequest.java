package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BagGrossRequest {

    @NotNull
    @DecimalMin("0")
    private BigDecimal grossValue;
}
