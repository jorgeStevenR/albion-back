package com.albion.guildbalance.web.security;

import com.albion.guildbalance.domain.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerPrincipal {

    private final Long playerId;
    private final String albionName;
    private final PlayerRole role;
}
