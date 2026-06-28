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
public class AvalonRolesOverviewResponse {

    private Long avalonId;
    private boolean registrationsOpen;
    private boolean avalonOpen;
    @Builder.Default
    private List<AvalonRoleSlotResponse> roles = new ArrayList<>();
}
