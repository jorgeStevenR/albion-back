package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.AvalonParticipantRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonParticipant;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonParticipantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AvalonParticipantRepositoryAdapter implements AvalonParticipantRepositoryPort {

    private final AvalonParticipantJpaRepository repository;

    @Override
    public List<AvalonParticipant> findByPlayerId(Long playerId) {
        return repository.findByPlayer_Id(playerId);
    }
}
