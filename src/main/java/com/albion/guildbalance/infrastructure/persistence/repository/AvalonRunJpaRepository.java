package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonRun;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvalonRunJpaRepository extends JpaRepository<AvalonRun, Long> {

    @Query("SELECT DISTINCT a FROM AvalonRun a LEFT JOIN FETCH a.participants p LEFT JOIN FETCH p.player ORDER BY a.date DESC, a.id DESC")
    List<AvalonRun> findAllWithParticipants();

    @Query("SELECT a FROM AvalonRun a LEFT JOIN FETCH a.participants p LEFT JOIN FETCH p.player WHERE a.id = :id")
    Optional<AvalonRun> findByIdWithParticipants(Long id);

    @Query("SELECT a FROM AvalonRun a LEFT JOIN FETCH a.lootItems WHERE a.id = :id")
    Optional<AvalonRun> findByIdWithLootItems(Long id);

    long countByStatus(AvalonStatus status);

    long countByDateGreaterThanEqual(LocalDate since);
}
