package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.Distribution;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DistributionJpaRepository extends JpaRepository<Distribution, Long> {

    long countByAvalonRun_Id(Long avalonRunId);

    @EntityGraph(attributePaths = {"avalonRun", "player"})
    List<Distribution> findByPlayer_Id(Long playerId);

    @EntityGraph(attributePaths = {"avalonRun", "player"})
    List<Distribution> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Distribution d WHERE d.createdAt >= :since")
    BigDecimal sumAmountSince(LocalDateTime since);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Distribution d WHERE d.player.id = :playerId")
    BigDecimal sumAmountByPlayer(Long playerId);

    @Query("SELECT d.player.id, COALESCE(SUM(d.amount), 0) FROM Distribution d WHERE d.player.id IN :playerIds GROUP BY d.player.id")
    List<Object[]> sumAmountByPlayerIds(@Param("playerIds") Collection<Long> playerIds);

    @Query("SELECT d.player.id, d.player.albionName, COALESCE(SUM(d.amount), 0) " +
            "FROM Distribution d WHERE d.createdAt >= :since " +
            "GROUP BY d.player.id, d.player.albionName " +
            "ORDER BY COALESCE(SUM(d.amount), 0) DESC")
    List<Object[]> sumAmountByPlayerSince(@Param("since") LocalDateTime since);
}
