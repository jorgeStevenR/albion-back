package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredPlayerSummary {

    private Long registrationId;
    private Long playerId;
    private String albionName;
    private String slotKey;
    private String slotDisplayName;
}
