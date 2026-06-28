package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerJpaRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByAlbionName(String albionName);

    Optional<Player> findByAlbionId(String albionId);

    boolean existsByAlbionName(String albionName);

    List<Player> findByGuild_Id(Long guildId);

    List<Player> findByGuildIsNotNull();

    long countByActiveTrueAndGuildIsNotNull();
}
