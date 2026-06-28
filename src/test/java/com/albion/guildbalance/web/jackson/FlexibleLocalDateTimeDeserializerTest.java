package com.albion.guildbalance.web.jackson;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FlexibleLocalDateTimeDeserializerTest {

    private final FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    @Test
    void parsesOffsetDateTime() throws Exception {
        try (var jsonParser = new com.fasterxml.jackson.core.JsonFactory().createParser("\"2026-06-28T18:44:00-05:00\"")) {
            jsonParser.nextToken();
            LocalDateTime result = deserializer.deserialize(jsonParser, null);
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 6, 28, 18, 44));
        }
    }

    @Test
    void parsesPlainLocalDateTime() throws Exception {
        try (var jsonParser = new com.fasterxml.jackson.core.JsonFactory().createParser("\"2026-06-28T18:44:00\"")) {
            jsonParser.nextToken();
            LocalDateTime result = deserializer.deserialize(jsonParser, null);
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 6, 28, 18, 44));
        }
    }
}
