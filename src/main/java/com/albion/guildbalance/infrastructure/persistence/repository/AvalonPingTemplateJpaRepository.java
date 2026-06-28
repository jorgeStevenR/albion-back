package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonPingTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvalonPingTemplateJpaRepository extends JpaRepository<AvalonPingTemplate, Long> {

    List<AvalonPingTemplate> findByActiveTrueOrderByNameAsc();

    List<AvalonPingTemplate> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"roleSlots", "createdBy"})
    @Query("SELECT t FROM AvalonPingTemplate t WHERE t.id = :id")
    Optional<AvalonPingTemplate> findWithSlotsById(@Param("id") Long id);
}
