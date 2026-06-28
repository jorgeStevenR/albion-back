package com.albion.guildbalance.domain.util;

import java.text.Normalizer;
import java.util.Locale;

public final class ItemSearchTextBuilder {

    private ItemSearchTextBuilder() {
    }

    public static String build(String displayName, String displayNameEs, String uniqueName) {
        StringBuilder text = new StringBuilder()
                .append(normalize(displayName)).append(' ')
                .append(normalize(displayNameEs)).append(' ')
                .append(normalize(uniqueName));

        ItemGameAliases.forUniqueName(uniqueName).ifPresent(entry -> {
            text.append(' ').append(normalize(entry.displayNameEs()));
            for (String alias : entry.searchAliases()) {
                text.append(' ').append(normalize(alias));
            }
        });

        return text.toString().trim();
    }

    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return Normalizer.normalize(input.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
