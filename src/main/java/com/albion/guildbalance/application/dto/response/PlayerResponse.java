package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {

    private Long id;
    private String albionName;
    private String discordName;
    private PlayerRole role;
    private LocalDateTime createdAt;
    private boolean active;
}
