package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.SaleRepositoryPort;
import com.albion.guildbalance.domain.entity.Sale;
import com.albion.guildbalance.infrastructure.persistence.repository.SaleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepositoryPort {

    private final SaleJpaRepository repository;

    @Override
    public Sale save(Sale sale) {
        return repository.save(sale);
    }
}
