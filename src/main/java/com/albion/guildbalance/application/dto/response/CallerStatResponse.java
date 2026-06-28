package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallerStatResponse {

    private Long playerId;
    private String playerName;
    private long avalonCount;
}
