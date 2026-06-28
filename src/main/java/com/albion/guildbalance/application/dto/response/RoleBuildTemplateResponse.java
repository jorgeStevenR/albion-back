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
public class RoleBuildTemplateResponse {

    private Long id;
    private RoleType roleType;
    private String name;
    private String description;
    @Builder.Default
    private List<RoleBuildSlotResponse> slots = new ArrayList<>();
}
