package com.albion.guildbalance.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Slf4j
@Configuration
public class AppTimeZoneConfig {

    @Value("${app.timezone:America/Bogota}")
    private String timezoneId;

    @PostConstruct
    void configureDefaultTimeZone() {
        TimeZone zone = TimeZone.getTimeZone(timezoneId);
        TimeZone.setDefault(zone);
        log.info("Application default timezone set to {}", zone.getID());
    }
}
