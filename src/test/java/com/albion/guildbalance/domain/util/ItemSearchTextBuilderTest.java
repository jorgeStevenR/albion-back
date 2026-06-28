package com.albion.guildbalance.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSearchTextBuilderTest {

    @Test
    void realmbreakerIncludesInGameSpanishAlias() {
        String searchText = ItemSearchTextBuilder.build(
                "Adept's Realmbreaker",
                "Romperreinos del iniciado",
                "T4_2H_AXE_AVALON");

        assertThat(searchText).contains("martillo relampago");
        assertThat(searchText).contains("realmbreaker");
        assertThat(searchText).contains("romperreinos");
    }

    @Test
    void artifactRealmbreakerDoesNotGetWeaponAlias() {
        String searchText = ItemSearchTextBuilder.build(
                "Adept's Avalonian Battle Memoir",
                "Recuerdos de batalla avalonianos del iniciado",
                "T4_ARTEFACT_2H_AXE_AVALON");

        assertThat(searchText).doesNotContain("martillo relampago");
    }
}
