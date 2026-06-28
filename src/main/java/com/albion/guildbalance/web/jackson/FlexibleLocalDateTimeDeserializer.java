package com.albion.guildbalance.web.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Accepts {@code 2026-06-28T18:44:00} or {@code 2026-06-28T18:44:00-05:00}.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Bogota");

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = parser.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();

        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(APP_ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(text);
        }
    }
}
