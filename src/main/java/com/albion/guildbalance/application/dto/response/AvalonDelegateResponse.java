package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AvalonDelegateResponse {

    private Long id;
    private Long playerId;
    private String playerName;
    private String assignedByName;
    private LocalDateTime createdAt;
}
