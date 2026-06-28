package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.Guild;

import java.util.Optional;

public interface GuildRepositoryPort {

    Guild save(Guild guild);

    Optional<Guild> findByAlbionGuildId(String albionGuildId);

    Optional<Guild> findByName(String name);

    Optional<Guild> findById(Long id);
}
