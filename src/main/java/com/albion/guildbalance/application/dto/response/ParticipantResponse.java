package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {

    private Long id;
    private Long playerId;
    private String albionName;
    private ParticipantType participantType;
    private String roleSlotKey;
    private String roleDisplayName;
}
