package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuildJpaRepository extends JpaRepository<Guild, Long> {

    Optional<Guild> findByAlbionGuildId(String albionGuildId);

    Optional<Guild> findByNameIgnoreCase(String name);
}
