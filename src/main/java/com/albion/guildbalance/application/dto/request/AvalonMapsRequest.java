package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonMapsRequest {

    @Min(0)
    private int mapsThrown;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal mapsCost;
}
