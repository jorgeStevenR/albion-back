package com.albion.guildbalance.domain.util;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildItemFilterTest {

    @Test
    void rejectsArtifactAndPrototypePieces() {
        assertThat(BuildItemFilter.isEquippableGear("T8_ARTEFACT_HEAD_PLATE_UNDEAD")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_HEAD_PLATE_PROTOTYPE")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_ARTEFACT_ARMOR_PLATE_UNDEAD")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T4_ARTEFACT_2H_AXE_AVALON")).isFalse();
    }

    @Test
    void rejectsGathererAndToolGear() {
        assertThat(BuildItemFilter.isEquippableGear("T8_ARMOR_GATHERER_FIBER")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_SHOES_GATHERER_HIDE")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_HEAD_GATHERER_WOOD")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_TOOL_HAMMER")).isFalse();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_TOOL_PICK")).isFalse();
    }

    @Test
    void acceptsWearableHeadChestAndShoes() {
        assertThat(BuildItemFilter.isEquippableGear("T8_HEAD_PLATE_SET1")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_HEAD_CLOTH_HELL")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_ARMOR_LEATHER_SET2")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_ARMOR_CLOTH_SET1")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_ARMOR_PLATE_ROYAL")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_SHOES_PLATE_SET1")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_SHOES_CLOTH_HELL")).isTrue();
    }

    @Test
    void acceptsMainhandWeaponFamilies() {
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_CURSEDSTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_FROSTSTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_MAIN_ARCANESTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_FIRESTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_MAIN_HOLYSTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_NATURESTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_SHAPESHIFTER_SET1")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_QUARTERSTAFF")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_SPEAR")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_DAGGERPAIR")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_DAGGERPAIR_CRYSTAL")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_MAIN_DAGGER")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_BOW")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_CROSSBOW")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_KNUCKLES_SET1")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_HAMMER")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_MAIN_MACE")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_2H_AXE")).isTrue();
        assertThat(BuildItemFilter.isEquippableGear("T8_MAIN_SWORD")).isTrue();
    }

    @Test
    void slotValidationMatchesClassifier() {
        assertThat(BuildItemFilter.isValidForSlot("T8_ARMOR_PLATE_SET1", EquipmentSlot.ARMOR)).isTrue();
        assertThat(BuildItemFilter.isValidForSlot("T8_2H_HAMMER", EquipmentSlot.MAINHAND)).isTrue();
        assertThat(BuildItemFilter.isValidForSlot("T8_2H_HAMMER", EquipmentSlot.ARMOR)).isFalse();
    }
}
