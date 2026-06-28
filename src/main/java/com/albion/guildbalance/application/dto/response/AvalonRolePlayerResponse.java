package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonRolePlayerResponse {

    private Long registrationId;
    private Long playerId;
    private String albionName;
}
