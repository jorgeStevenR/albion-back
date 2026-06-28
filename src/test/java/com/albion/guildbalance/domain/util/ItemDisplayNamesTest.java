package com.albion.guildbalance.domain.util;

import com.albion.guildbalance.domain.entity.AlbionItem;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemDisplayNamesTest {

    @Test
    void usesSpanishFromCatalogWhenAvailable() {
        AlbionItem item = AlbionItem.builder()
                .uniqueName("T8_2H_HAMMER")
                .displayName("Elder's Great Hammer")
                .displayNameEs("Gran martillo del anciano")
                .equipmentSlot(EquipmentSlot.MAINHAND)
                .tier(8)
                .enchantment(0)
                .build();

        assertThat(ItemDisplayNames.spanishName(item)).isEqualTo("Gran martillo del anciano");
        assertThat(ItemDisplayNames.label(item)).isEqualTo("Gran martillo del anciano · T8.0");
    }

    @Test
    void usesGameAliasForRealmbreaker() {
        AlbionItem item = AlbionItem.builder()
                .uniqueName("T4_2H_AXE_AVALON")
                .displayName("Adept's Realmbreaker")
                .displayNameEs("Romperreinos del iniciado")
                .equipmentSlot(EquipmentSlot.MAINHAND)
                .tier(4)
                .enchantment(0)
                .build();

        assertThat(ItemDisplayNames.spanishName(item)).isEqualTo("Martillo Relámpago del Iniciado");
    }
}
