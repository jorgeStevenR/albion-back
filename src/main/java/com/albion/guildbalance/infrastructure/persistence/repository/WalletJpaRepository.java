package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByPlayer_Id(Long playerId);

    List<Wallet> findByPlayer_IdIn(Collection<Long> playerIds);

    @Query("SELECT w.player.id, w.balance FROM Wallet w WHERE w.player.id IN :playerIds")
    List<Object[]> findBalancesByPlayerIds(@Param("playerIds") Collection<Long> playerIds);

    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM Wallet w")
    BigDecimal sumAllBalances();
}
