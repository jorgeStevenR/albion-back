package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long playerId;
    private String albionName;
    private PlayerRole role;
}
