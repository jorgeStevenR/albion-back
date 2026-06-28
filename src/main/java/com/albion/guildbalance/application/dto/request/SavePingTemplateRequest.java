package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class SavePingTemplateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String zone;

    private String description;

    private String pingMessage;

    @NotEmpty
    @Valid
    private List<PingTemplateRoleSlotRequest> roleSlots;
}
