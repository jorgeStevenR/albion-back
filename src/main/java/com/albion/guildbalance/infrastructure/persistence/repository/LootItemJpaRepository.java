package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.LootType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface LootItemJpaRepository extends JpaRepository<LootItem, Long> {

    void deleteByAvalonRun_IdAndType(Long avalonRunId, LootType type);

    @Query("""
            SELECT COALESCE(SUM(l.marketValue * l.quantity), 0)
            FROM LootItem l
            WHERE l.saleStatus = :status
            """)
    BigDecimal sumMarketValueBySaleStatus(LootSaleStatus status);

    long countByAvalonRunIdAndSaleStatus(Long avalonRunId, LootSaleStatus status);
}
