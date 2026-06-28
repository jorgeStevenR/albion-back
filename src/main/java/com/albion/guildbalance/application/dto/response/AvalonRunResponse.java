package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.AvalonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonRunResponse {

    private Long id;
    private LocalDate date;
    private LocalDateTime scheduledAt;
    private String zone;
    private String description;
    private AvalonStatus status;
    private boolean registrationsOpen;
    private int mapsThrown;
    private java.math.BigDecimal mapsCost;
    private List<ParticipantResponse> participants;
    private List<LootItemResponse> lootItems;
    private Long createdByPlayerId;
    private String createdByName;
    private int registeredCount;
    private int totalCapacity;
}
