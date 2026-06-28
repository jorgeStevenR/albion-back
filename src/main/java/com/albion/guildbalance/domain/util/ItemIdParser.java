package com.albion.guildbalance.domain.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemIdParser {

    private static final Pattern TIER_PATTERN = Pattern.compile("^T([1-8])_");
    private static final Pattern ENCHANT_PATTERN = Pattern.compile("@([0-4])$");

    private ItemIdParser() {
    }

    public static int parseTier(String uniqueName) {
        if (uniqueName == null) {
            return 0;
        }
        Matcher matcher = TIER_PATTERN.matcher(uniqueName.toUpperCase(Locale.ROOT));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public static int parseEnchantment(String uniqueName) {
        if (uniqueName == null) {
            return 0;
        }
        Matcher matcher = ENCHANT_PATTERN.matcher(uniqueName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
