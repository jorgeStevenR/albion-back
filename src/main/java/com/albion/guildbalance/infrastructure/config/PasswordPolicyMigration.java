package com.albion.guildbalance.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time migration: when must_change_password column was added, Hibernate defaulted
 * existing players to true and blocked all API access. Existing roster keeps their passwords;
 * only players created after this migration get forced change (guild sync, admin create).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordPolicyMigration implements ApplicationRunner {

    private static final String MIGRATION_ID = "password_policy_v1";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureMigrationsTable();
        if (isApplied()) {
            return;
        }

        int updated = jdbcTemplate.update("UPDATE players SET must_change_password = false");
        markApplied();
        log.info("Password policy v1 applied — {} existing players exempt from forced password change", updated);
    }

    private void ensureMigrationsTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_migrations (
                    id VARCHAR(64) PRIMARY KEY,
                    applied_at TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """);
    }

    private boolean isApplied() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_migrations WHERE id = ?",
                Integer.class,
                MIGRATION_ID);
        return count != null && count > 0;
    }

    private void markApplied() {
        jdbcTemplate.update("INSERT INTO app_migrations (id) VALUES (?)", MIGRATION_ID);
    }
}
