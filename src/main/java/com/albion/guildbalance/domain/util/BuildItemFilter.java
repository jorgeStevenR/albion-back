package com.albion.guildbalance.domain.util;

import com.albion.guildbalance.domain.enums.EquipmentSlot;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Filters ao-bin-dumps items to equippable combat/gathering PvP gear for build templates.
 * Excludes crafting artifacts, prototypes, gatherer work gear and tools.
 */
public final class BuildItemFilter {

    private static final Pattern COMBAT_MAINHAND = Pattern.compile(
            "STAFF|QUARTERSTAFF|SHAPESHIFTER|"
                    + "SPEAR|GLAIVE|HALBERD|TRIDENT|POLEHAMMER|"
                    + "DAGGER|CLAWPAIR|KATAR|"
                    + "BOW|CROSSBOW|WARBOW|LONGBOW|REPEATINGCROSSBOW|"
                    + "KNUCKLES|"
                    + "HAMMER|MACE|FLAIL|DUALHAMMER|"
                    + "AXE|DUALAXE|SCYTHE|CLEAVER|TWINSCYTHE|DUALSICKLE|"
                    + "SWORD|CLAYMORE|DUALSWORD|DUALSCIMITAR|"
                    + "DUALCROSSBOW|DUALMACE|"
                    + "WILDSTAFF|DEMONICSTAFF|DIVINESTAFF|INFERNOSTAFF|ENIGMATICSTAFF|"
                    + "GLACIALSTAFF|ROCKSTAFF|COMBATSTAFF|IRONCLADEDSTAFF|DOUBLEBLADEDSTAFF|"
                    + "FIRE_RINGPAIR|ARCANE_RINGPAIR|ICEGAUNTLETS|IRONGAUNTLETS|"
                    + "HARPOON|RAM_KEEPER",
            Pattern.CASE_INSENSITIVE);

    private BuildItemFilter() {
    }

    public static boolean isEquippableGear(String uniqueName) {
        if (uniqueName == null || uniqueName.isBlank()) {
            return false;
        }
        if (!passesGlobalExcludes(uniqueName)) {
            return false;
        }
        EquipmentSlot slot = ItemSlotClassifier.classify(uniqueName);
        if (slot == null) {
            return false;
        }
        return isValidForSlot(uniqueName, slot);
    }

    private static boolean passesGlobalExcludes(String uniqueName) {
        String upper = uniqueName.toUpperCase(Locale.ROOT);
        if (upper.contains("_ARTEFACT_")) {
            return false;
        }
        if (upper.contains("PROTOTYPE")) {
            return false;
        }
        if (upper.contains("GATHERER")) {
            return false;
        }
        if (upper.contains("_TOOL_")) {
            return false;
        }
        if (upper.contains("AVATAR")) {
            return false;
        }
        return true;
    }

    static boolean isValidForSlot(String uniqueName, EquipmentSlot slot) {
        String upper = uniqueName.toUpperCase(Locale.ROOT);
        return switch (slot) {
            case HEAD -> upper.contains("_HEAD") || upper.contains("_HELMET");
            case ARMOR -> upper.contains("_ARMOR");
            case SHOES -> upper.contains("_SHOES") || upper.endsWith("_SHOE");
            case MAINHAND -> COMBAT_MAINHAND.matcher(upper).find();
            case OFFHAND -> upper.contains("_OFF")
                    || upper.contains("SHIELD")
                    || upper.contains("TORCH")
                    || upper.contains("_ORB")
                    || upper.contains("_BOOK")
                    || upper.contains("_HORN")
                    || upper.contains("_MIST")
                    || upper.contains("_CENSER");
            case CAPE -> upper.contains("_CAPE");
            case BAG -> upper.contains("_BAG") || upper.contains("_SATCHEL");
            case MOUNT -> upper.contains("_MOUNT") || upper.contains("HORSE");
            case FOOD -> upper.contains("_MEAL");
            case POTION -> upper.contains("_POTION");
        };
    }
}
