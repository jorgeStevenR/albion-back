package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.GuildMoneyRequest;
import com.albion.guildbalance.domain.enums.MoneyRequestStatus;
import com.albion.guildbalance.domain.enums.MoneyRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuildMoneyRequestJpaRepository extends JpaRepository<GuildMoneyRequest, Long> {

    List<GuildMoneyRequest> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    List<GuildMoneyRequest> findByPlayerIdAndTypeOrderByCreatedAtDesc(Long playerId, MoneyRequestType type);

    List<GuildMoneyRequest> findByTypeOrderByCreatedAtDesc(MoneyRequestType type);

    List<GuildMoneyRequest> findAllByOrderByCreatedAtDesc();

    long countByTypeAndStatus(MoneyRequestType type, MoneyRequestStatus status);
}
