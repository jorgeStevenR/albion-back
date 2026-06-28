package com.albion.guildbalance.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class AlbionItemSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("albion_items")) {
            return;
        }

        addColumnIfMissing("display_name_es", "VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing("search_text", "VARCHAR(512) NOT NULL DEFAULT ''");
        addColumnIfMissing("enchantment", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("quality", "INTEGER NOT NULL DEFAULT 1");

        jdbcTemplate.update("""
                UPDATE albion_items
                SET display_name_es = display_name
                WHERE display_name_es IS NULL OR display_name_es = ''
                """);

        log.info("Albion items schema migration applied");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void addColumnIfMissing(String columnName, String columnDefinition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'albion_items'
                  AND column_name = ?
                """, Integer.class, columnName);

        if (count != null && count > 0) {
            return;
        }

        log.info("Adding missing column albion_items.{}", columnName);
        jdbcTemplate.execute("ALTER TABLE albion_items ADD COLUMN " + columnName + " " + columnDefinition);
    }
}
