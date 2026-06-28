package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.LootItem;

import java.util.Optional;

public interface LootItemRepositoryPort {

    Optional<LootItem> findById(Long id);

    LootItem save(LootItem lootItem);
}
