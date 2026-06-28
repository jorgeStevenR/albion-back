package com.albion.guildbalance.domain.util;

import com.albion.guildbalance.domain.enums.EquipmentSlot;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ItemSlotClassifier {

    private static final Pattern TIER_PATTERN = Pattern.compile("^T([1-8])_");

    private ItemSlotClassifier() {
    }

    public static EquipmentSlot classify(String uniqueName) {
        if (uniqueName == null || uniqueName.isBlank()) {
            return null;
        }
        String upper = uniqueName.toUpperCase(Locale.ROOT);

        if (upper.contains("_SHOES") || upper.endsWith("_SHOE")) {
            return EquipmentSlot.SHOES;
        }
        if (upper.contains("_HEAD") || upper.contains("_HELMET")) {
            return EquipmentSlot.HEAD;
        }
        if (upper.contains("_ARMOR")) {
            return EquipmentSlot.ARMOR;
        }
        if (upper.contains("_CAPE")) {
            return EquipmentSlot.CAPE;
        }
        if (upper.contains("_BAG") || upper.contains("_SATCHEL")) {
            return EquipmentSlot.BAG;
        }
        if (upper.contains("_MOUNT") || upper.contains("HORSE")) {
            return EquipmentSlot.MOUNT;
        }
        if (upper.contains("_MEAL") || upper.contains("_FISH") && upper.contains("T")) {
            return EquipmentSlot.FOOD;
        }
        if (upper.contains("_POTION")) {
            return EquipmentSlot.POTION;
        }
        if (upper.contains("_OFF") || upper.contains("SHIELD") || upper.contains("TORCH")
                || upper.contains("_ORB") || upper.contains("_BOOK") || upper.contains("_HORN")
                || upper.contains("_MIST") || upper.contains("_CENSER")) {
            return EquipmentSlot.OFFHAND;
        }
        if (isMainhand(upper)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }

    public static int parseTier(String uniqueName) {
        if (uniqueName == null) {
            return 0;
        }
        var matcher = TIER_PATTERN.matcher(uniqueName.toUpperCase(Locale.ROOT));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private static boolean isMainhand(String upper) {
        return upper.contains("_2H_") || upper.contains("_1H_") || upper.contains("_MAIN_")
                || upper.contains("STAFF") || upper.contains("_BOW") || upper.contains("CROSSBOW")
                || upper.contains("DAGGER") || upper.contains("HAMMER") || upper.contains("_MACE")
                || upper.contains("_AXE") || upper.contains("_SPEAR") || upper.contains("_SWORD")
                || upper.contains("KNUCKLES") || upper.contains("QUARTERSTAFF");
    }

    public static boolean isTwoHandedWeapon(String uniqueName) {
        if (uniqueName == null || uniqueName.isBlank()) {
            return false;
        }
        String upper = uniqueName.toUpperCase(Locale.ROOT);
        return upper.contains("_2H_") || upper.contains("_MAIN_TWOHAND");
    }
}
