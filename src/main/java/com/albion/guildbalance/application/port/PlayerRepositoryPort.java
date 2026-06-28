package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.*;

import java.util.List;
import java.util.Optional;

public interface PlayerRepositoryPort {

    Player save(Player player);

    Optional<Player> findById(Long id);

    Optional<Player> findByAlbionName(String albionName);

    Optional<Player> findByAlbionId(String albionId);

    List<Player> findAll();

    List<Player> findAllByGuildId(Long guildId);

    List<Player> findAllWithGuild();

    boolean existsByAlbionName(String albionName);

    void delete(Player player);
}
