package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.domain.entity.Wallet;

import java.util.Optional;

public interface WalletRepositoryPort {

    Wallet save(Wallet wallet);

    Optional<Wallet> findByPlayerId(Long playerId);
}
