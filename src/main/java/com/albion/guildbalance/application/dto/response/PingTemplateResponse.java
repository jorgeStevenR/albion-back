package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PingTemplateResponse {

    private Long id;
    private String name;
    private String zone;
    private String description;
    private String pingMessage;
    private boolean active;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<PingTemplateRoleSlotResponse> roleSlots;
}
