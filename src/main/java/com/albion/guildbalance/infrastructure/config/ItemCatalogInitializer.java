package com.albion.guildbalance.infrastructure.config;

import com.albion.guildbalance.application.service.AlbionItemCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ItemCatalogInitializer {

    private final AlbionItemCatalogService itemCatalogService;

    @Bean
    CommandLineRunner initItemCatalog() {
        return args -> Thread.startVirtualThread(() -> {
            try {
                log.info("Loading Albion item catalog (ES names) in background...");
                int count = itemCatalogService.syncCatalogIfEmpty();
                log.info("Albion item catalog ready ({} items)", count);
            } catch (Exception ex) {
                log.warn("Could not sync Albion item catalog on startup: {}", ex.getMessage(), ex);
            }
        });
    }
}
