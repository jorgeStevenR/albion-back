package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionCalculationResponse {

    private Long avalonId;
    private BigDecimal totalBalance;
    private BigDecimal bagNet;
    private BigDecimal chestNet;
    private BigDecimal mapsDeducted;
    private Double totalWeight;
    private List<DistributionResponse> distributions;
}
