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
public class GuildPlayerDetailResponse {

    private Long id;
    private String albionName;
    private String rank;
    private boolean active;
    private String guildName;
    private BigDecimal balance;
    private List<DistributionResponse> distributions;
    private List<AvalonParticipationResponse> avalonParticipations;
}
