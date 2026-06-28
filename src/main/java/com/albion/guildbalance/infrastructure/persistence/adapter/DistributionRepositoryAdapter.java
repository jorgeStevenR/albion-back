package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.DistributionRepositoryPort;
import com.albion.guildbalance.domain.entity.Distribution;
import com.albion.guildbalance.infrastructure.persistence.repository.DistributionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DistributionRepositoryAdapter implements DistributionRepositoryPort {

    private final DistributionJpaRepository repository;

    @Override
    public Distribution save(Distribution distribution) {
        return repository.save(distribution);
    }

    @Override
    public List<Distribution> findByPlayerId(Long playerId) {
        return repository.findByPlayer_Id(playerId);
    }
}
