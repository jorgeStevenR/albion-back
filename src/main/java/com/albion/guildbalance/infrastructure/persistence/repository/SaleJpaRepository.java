package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.Sale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleJpaRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"lootItem", "lootItem.avalonRun", "buyer"})
    List<Sale> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(s.finalPrice), 0) FROM Sale s")
    BigDecimal sumAllFinalPrices();

    @Query("SELECT COALESCE(SUM(s.finalPrice), 0) FROM Sale s WHERE s.createdAt >= :since")
    BigDecimal sumFinalPricesSince(LocalDateTime since);
}
