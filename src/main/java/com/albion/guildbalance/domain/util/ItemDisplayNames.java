package com.albion.guildbalance.domain.util;

import com.albion.guildbalance.domain.entity.AlbionItem;

public final class ItemDisplayNames {

    private ItemDisplayNames() {
    }

    public static String spanishName(AlbionItem item) {
        return ItemGameAliases.forUniqueName(item.getUniqueName())
                .map(ItemGameAliases.AliasEntry::displayNameEs)
                .orElseGet(() -> spanishName(item.getDisplayNameEs(), item.getDisplayName()));
    }

    public static String spanishName(String displayNameEs, String displayName) {
        if (displayNameEs != null && !displayNameEs.isBlank()
                && !displayNameEs.equalsIgnoreCase(displayName)) {
            return displayNameEs;
        }
        return displayName != null ? displayName : "";
    }

    public static String label(AlbionItem item) {
        String base = spanishName(item);
        int enchant = item.getEnchantment();
        return base + " · T" + item.getTier() + (enchant > 0 ? "." + enchant : ".0");
    }
}
