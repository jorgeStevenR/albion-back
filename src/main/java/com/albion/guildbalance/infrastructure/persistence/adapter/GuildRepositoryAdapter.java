package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.GuildRepositoryPort;
import com.albion.guildbalance.domain.entity.Guild;
import com.albion.guildbalance.infrastructure.persistence.repository.GuildJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GuildRepositoryAdapter implements GuildRepositoryPort {

    private final GuildJpaRepository repository;

    @Override
    public Guild save(Guild guild) {
        return repository.save(guild);
    }

    @Override
    public Optional<Guild> findByAlbionGuildId(String albionGuildId) {
        return repository.findByAlbionGuildId(albionGuildId);
    }

    @Override
    public Optional<Guild> findByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    @Override
    public Optional<Guild> findById(Long id) {
        return repository.findById(id);
    }
}
