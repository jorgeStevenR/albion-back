package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.PenaltyAppeal;
import com.albion.guildbalance.domain.enums.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PenaltyAppealJpaRepository extends JpaRepository<PenaltyAppeal, Long> {

    Optional<PenaltyAppeal> findByPenaltyId(Long penaltyId);

    List<PenaltyAppeal> findByStatusOrderByCreatedAtAsc(AppealStatus status);

    List<PenaltyAppeal> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    List<PenaltyAppeal> findAllByOrderByCreatedAtDesc();
}
