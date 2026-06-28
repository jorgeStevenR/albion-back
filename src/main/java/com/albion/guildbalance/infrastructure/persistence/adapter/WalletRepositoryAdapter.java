package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepositoryPort {

    private final WalletJpaRepository repository;

    @Override
    public Wallet save(Wallet wallet) {
        return repository.save(wallet);
    }

    @Override
    public Optional<Wallet> findByPlayerId(Long playerId) {
        return repository.findByPlayer_Id(playerId);
    }
}
