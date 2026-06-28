package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonRoleSlotResponse {

    private Long slotId;
    private String slotKey;
    private String displayName;
    private RoleType roleType;
    private int maxPlayers;
    private int currentPlayers;
    private int sortOrder;
    @Builder.Default
    private List<AvalonRolePlayerResponse> players = new ArrayList<>();
    private boolean full;
    private Long currentPlayerRegistrationId;
    private RoleBuildTemplateResponse buildTemplate;
    @Builder.Default
    private List<RoleBuildSlotResponse> slotBuild = new ArrayList<>();
    @Builder.Default
    private List<SwapItemResponse> slotSwaps = new ArrayList<>();
}
