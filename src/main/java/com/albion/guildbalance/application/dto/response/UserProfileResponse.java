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
public class UserProfileResponse {

    private Long playerId;
    private String albionName;
    private String discordName;
    private String rank;
    private PlayerRole role;
    private boolean mustChangePassword;
}
