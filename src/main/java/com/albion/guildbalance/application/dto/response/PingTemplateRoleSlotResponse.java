package com.albion.guildbalance.application.dto.response;

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
public class PingTemplateRoleSlotResponse {

    private String slotKey;
    private String displayName;
    private Integer maxPlayers;
    private Integer sortOrder;
    @Builder.Default
    private List<RoleBuildSlotResponse> buildSlots = new ArrayList<>();
    @Builder.Default
    private List<SwapItemResponse> swapItems = new ArrayList<>();
}
