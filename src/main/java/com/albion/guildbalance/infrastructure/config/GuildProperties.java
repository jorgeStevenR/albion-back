package com.albion.guildbalance.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "albion.guild")
public class GuildProperties {

    private String name = "II TEMPUS FUGIT II";
    private String defaultMemberPassword = "tempus123";
    private boolean autoSyncOnStartup = true;
    /** Minutos sin re-sincronizar si ya hay datos frescos (6 h por defecto) */
    private int syncCacheMinutes = 360;
    /** Cron: cada 6 horas por defecto */
    private String autoSyncCron = "0 0 */6 * * *";
}
