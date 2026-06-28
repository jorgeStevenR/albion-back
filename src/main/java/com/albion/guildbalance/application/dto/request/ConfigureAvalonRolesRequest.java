package com.albion.guildbalance.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureAvalonRolesRequest {

    @NotEmpty
    @Valid
    private List<AvalonRoleSlotRequest> slots;
}
