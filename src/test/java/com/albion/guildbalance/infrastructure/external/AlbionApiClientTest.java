package com.albion.guildbalance.infrastructure.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlbionApiClientTest {

    @Test
    @DisplayName("Normalize guild name strips decorative pipes and extra spaces")
    void normalizeGuildName_stripsPipes() {
        assertEquals("TEMPUS FUGIT", AlbionApiClient.normalizeGuildName("|| TEMPUS FUGIT ||"));
        assertEquals("Mi Guild", AlbionApiClient.normalizeGuildName("  Mi   Guild  "));
    }
}
