package com.albion.guildbalance.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PingTemplateRoleSlotRequest {

    @NotBlank
    private String slotKey;

    @NotBlank
    private String displayName;

    @NotNull
    @Min(0)
    private Integer maxPlayers;

    @NotNull
    private Integer sortOrder;

    @Valid
    private List<RoleBuildSlotRequest> buildSlots = new ArrayList<>();

    @Valid
    private List<SwapItemRequest> swapItems = new ArrayList<>();
}
