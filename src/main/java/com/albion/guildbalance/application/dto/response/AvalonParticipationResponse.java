package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonParticipationResponse {

    private Long avalonId;
    private LocalDate date;
    private String zone;
    private ParticipantType participantType;
}
