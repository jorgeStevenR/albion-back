package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonPenalty;
import com.albion.guildbalance.domain.enums.PenaltyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvalonPenaltyJpaRepository extends JpaRepository<AvalonPenalty, Long> {

    List<AvalonPenalty> findByAvalonRunIdOrderByCreatedAtDesc(Long avalonId);

    List<AvalonPenalty> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    List<AvalonPenalty> findByPlayerIdAndStatusOrderByCreatedAtDesc(Long playerId, PenaltyStatus status);

    List<AvalonPenalty> findAllByOrderByCreatedAtDesc();
}
