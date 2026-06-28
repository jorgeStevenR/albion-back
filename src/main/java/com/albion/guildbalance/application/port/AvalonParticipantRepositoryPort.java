package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.AvalonParticipant;

import java.util.List;

public interface AvalonParticipantRepositoryPort {

    List<AvalonParticipant> findByPlayerId(Long playerId);
}
