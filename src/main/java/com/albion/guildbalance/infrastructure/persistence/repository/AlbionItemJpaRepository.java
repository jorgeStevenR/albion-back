package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AlbionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AlbionItemJpaRepository extends JpaRepository<AlbionItem, String>, JpaSpecificationExecutor<AlbionItem> {

    long count();

    @Query("""
            SELECT COUNT(i) FROM AlbionItem i
            WHERE i.displayNameEs IS NULL OR i.displayNameEs = '' OR i.searchText IS NULL OR i.searchText = ''
            """)
    long countMissingSpanishNames();

    @Query("""
            SELECT COUNT(i) FROM AlbionItem i
            WHERE i.uniqueName IN ('T8_2H_HAMMER', 'T4_2H_AXE_AVALON')
              AND (
                i.displayNameEs IS NULL OR i.displayNameEs = ''
                OR LOWER(i.displayNameEs) = LOWER(i.displayName)
                OR (i.uniqueName = 'T8_2H_HAMMER' AND LOWER(i.displayNameEs) NOT LIKE '%martillo%')
                OR (i.uniqueName = 'T4_2H_AXE_AVALON' AND i.searchText NOT LIKE '%martillo relampago%')
              )
            """)
    long countStaleCatalog();

    @Query("""
            SELECT COUNT(i) FROM AlbionItem i
            WHERE i.uniqueName LIKE '%ARTEFACT%'
               OR i.uniqueName LIKE '%PROTOTYPE%'
               OR i.uniqueName LIKE '%GATHERER%'
               OR i.uniqueName LIKE '%_TOOL_%'
            """)
    long countCraftingArtifacts();

    @Modifying
    @Query(value = "TRUNCATE TABLE albion_items", nativeQuery = true)
    void truncateAll();
}
