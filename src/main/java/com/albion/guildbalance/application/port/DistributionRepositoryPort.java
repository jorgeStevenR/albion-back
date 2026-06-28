package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.Distribution;

import java.util.List;

public interface DistributionRepositoryPort {

    Distribution save(Distribution distribution);

    List<Distribution> findByPlayerId(Long playerId);
}
