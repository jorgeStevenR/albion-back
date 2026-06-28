package com.albion.guildbalance.application.service;

import com.albion.guildbalance.domain.entity.AlbionItem;
import com.albion.guildbalance.infrastructure.persistence.repository.AlbionItemJdbcRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.AlbionItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbionItemCatalogSyncHelper {

    private final AlbionItemJpaRepository repository;
    private final AlbionItemJdbcRepository jdbcRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearAll() {
        repository.truncateAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveAllInBatches(List<AlbionItem> items) {
        return jdbcRepository.insertAll(items);
    }
}
