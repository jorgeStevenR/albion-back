package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.infrastructure.persistence.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlayerRepositoryAdapter implements PlayerRepositoryPort {

    private final PlayerJpaRepository repository;

    @Override
    public Player save(Player player) {
        return repository.save(player);
    }

    @Override
    public Optional<Player> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Player> findByAlbionName(String albionName) {
        return repository.findByAlbionName(albionName);
    }

    @Override
    public Optional<Player> findByAlbionId(String albionId) {
        return repository.findByAlbionId(albionId);
    }

    @Override
    public List<Player> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Player> findAllByGuildId(Long guildId) {
        return repository.findByGuild_Id(guildId);
    }

    @Override
    public List<Player> findAllWithGuild() {
        return repository.findByGuildIsNotNull();
    }

    @Override
    public boolean existsByAlbionName(String albionName) {
        return repository.existsByAlbionName(albionName);
    }

    @Override
    public void delete(Player player) {
        repository.delete(player);
    }
}
