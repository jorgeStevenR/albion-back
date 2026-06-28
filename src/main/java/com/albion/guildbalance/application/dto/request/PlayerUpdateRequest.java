package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.PlayerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdateRequest {

    @NotBlank(message = "Albion name is required")
    private String albionName;

    @NotBlank(message = "Discord name is required")
    private String discordName;

    @NotNull(message = "Role is required")
    private PlayerRole role;

    private String password;
}
