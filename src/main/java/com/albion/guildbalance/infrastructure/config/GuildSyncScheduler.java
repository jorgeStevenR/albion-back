package com.albion.guildbalance.infrastructure.config;

import com.albion.guildbalance.application.service.GuildSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class GuildSyncScheduler {

    private final GuildSyncService guildSyncService;
    private final GuildProperties guildProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        if (!guildProperties.isAutoSyncOnStartup()) {
            return;
        }
        runSync("startup");
    }

    @Scheduled(cron = "${albion.guild.auto-sync-cron:0 0 */6 * * *}")
    public void syncScheduled() {
        runSync("scheduled");
    }

    private void runSync(String trigger) {
        try {
            var result = guildSyncService.syncConfiguredGuildIfStale();
            if (result.isSkipped()) {
                log.info("Guild sync ({}) skipped for {} — {} members in cache (last sync: {})",
                        trigger, result.getGuild(), result.getPlayersImported(), result.getLastSyncAt());
            } else {
                log.info("Guild sync ({}) for {} — created: {}, updated: {}",
                        trigger, result.getGuild(), result.getCreated(), result.getUpdated());
            }
        } catch (Exception ex) {
            log.warn("Guild sync ({}) failed: {}", trigger, ex.getMessage());
        }
    }
}
