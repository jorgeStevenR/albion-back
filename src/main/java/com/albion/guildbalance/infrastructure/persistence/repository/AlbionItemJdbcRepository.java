package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AlbionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AlbionItemJdbcRepository {

    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    public int insertAll(List<AlbionItem> items) {
        jdbcTemplate.batchUpdate(
                """
                INSERT INTO albion_items
                    (unique_name, display_name, display_name_es, equipment_slot, tier, enchantment, quality, search_text)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                items,
                BATCH_SIZE,
                (ps, item) -> {
                    ps.setString(1, item.getUniqueName());
                    ps.setString(2, item.getDisplayName());
                    ps.setString(3, item.getDisplayNameEs());
                    ps.setString(4, item.getEquipmentSlot().name());
                    ps.setInt(5, item.getTier());
                    ps.setInt(6, item.getEnchantment());
                    ps.setInt(7, item.getQuality());
                    ps.setString(8, item.getSearchText());
                });
        return items.size();
    }
}
