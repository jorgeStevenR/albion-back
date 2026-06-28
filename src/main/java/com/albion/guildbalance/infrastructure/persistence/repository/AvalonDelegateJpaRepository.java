package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonDelegate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvalonDelegateJpaRepository extends JpaRepository<AvalonDelegate, Long> {

    List<AvalonDelegate> findByAvalonRunId(Long avalonId);

    boolean existsByAvalonRunIdAndPlayerId(Long avalonId, Long playerId);

    void deleteByAvalonRunIdAndPlayerId(Long avalonId, Long playerId);
}
