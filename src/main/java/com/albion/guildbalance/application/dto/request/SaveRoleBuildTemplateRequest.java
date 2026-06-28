package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SaveRoleBuildTemplateRequest {

    @NotBlank
    private String name;

    private String description;

    @Valid
    @Builder.Default
    private List<RoleBuildSlotRequest> slots = new ArrayList<>();
}
