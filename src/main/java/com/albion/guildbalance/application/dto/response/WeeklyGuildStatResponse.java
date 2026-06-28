package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WeeklyGuildStatResponse {

    private String weekLabel;
    private long avalonCount;
    private BigDecimal salesIncome;
    private BigDecimal memberEarnings;
}
