package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.RoleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonRoleSlotRequest {

    @NotNull
    private RoleType roleType;

    @Min(0)
    private int maxPlayers;
}
