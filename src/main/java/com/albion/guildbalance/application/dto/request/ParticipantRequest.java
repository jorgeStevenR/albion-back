package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.ParticipantType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequest {

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Participant type is required")
    private ParticipantType participantType;
}
