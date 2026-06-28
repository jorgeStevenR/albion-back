package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.AvalonRunRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRun;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonRunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AvalonRunRepositoryAdapter implements AvalonRunRepositoryPort {

    private final AvalonRunJpaRepository repository;

    @Override
    public AvalonRun save(AvalonRun avalonRun) {
        return repository.save(avalonRun);
    }

    @Override
    public Optional<AvalonRun> findById(Long id) {
        return repository.findByIdWithParticipants(id).map(avalon -> {
            repository.findByIdWithLootItems(id)
                    .ifPresent(withLoot -> avalon.setLootItems(withLoot.getLootItems()));
            return avalon;
        });
    }

    @Override
    public List<AvalonRun> findAll() {
        return repository.findAllWithParticipants();
    }

    @Override
    public Optional<AvalonRun> findLatestScheduledByCreator(Long creatorId) {
        return repository.findFirstByCreatedByIdAndScheduledAtIsNotNullOrderByScheduledAtDesc(creatorId);
    }
}
