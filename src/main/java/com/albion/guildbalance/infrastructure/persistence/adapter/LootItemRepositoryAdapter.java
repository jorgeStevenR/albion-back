package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.LootItemRepositoryPort;
import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.infrastructure.persistence.repository.LootItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LootItemRepositoryAdapter implements LootItemRepositoryPort {

    private final LootItemJpaRepository repository;

    @Override
    public Optional<LootItem> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public LootItem save(LootItem lootItem) {
        return repository.save(lootItem);
    }
}
