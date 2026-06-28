package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AvalonParticipantJpaRepository extends JpaRepository<AvalonParticipant, Long> {

    @EntityGraph(attributePaths = {"avalonRun"})
    List<AvalonParticipant> findByPlayer_Id(Long playerId);

    @Query("SELECT p.player.id, COUNT(p) FROM AvalonParticipant p WHERE p.player.id IN :playerIds GROUP BY p.player.id")
    List<Object[]> countByPlayerIds(@Param("playerIds") Collection<Long> playerIds);
}
