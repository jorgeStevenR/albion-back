package com.albion.guildbalance.domain.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * In-game Spanish names that differ from ao-bin-dumps (ES-ES) exports.
 * Keeps search and display aligned with what players see in the client.
 */
public final class ItemGameAliases {

    private static final Pattern REALMBREAKER = Pattern.compile("^T[4-8]_2H_AXE_AVALON(@[0-4])?$");

    private static final Map<Integer, String> TIER_ES_LABEL = Map.of(
            4, "Iniciado",
            5, "Experto",
            6, "Maestro",
            7, "Gran maestro",
            8, "Anciano"
    );

    private ItemGameAliases() {
    }

    public record AliasEntry(String displayNameEs, List<String> searchAliases) {
    }

    public static Optional<AliasEntry> forUniqueName(String uniqueName) {
        if (uniqueName == null || uniqueName.isBlank()) {
            return Optional.empty();
        }
        if (REALMBREAKER.matcher(uniqueName).matches()) {
            int tier = ItemIdParser.parseTier(uniqueName);
            String tierLabel = TIER_ES_LABEL.getOrDefault(tier, "Iniciado");
            String tierLabelSearch = tierLabel.toLowerCase(java.util.Locale.ROOT);
            String display = "Martillo Relámpago del " + tierLabel;
            List<String> aliases = new ArrayList<>(List.of(
                    "martillo relampago",
                    "martillo relampago del " + tierLabelSearch,
                    "realmbreaker",
                    "romperreinos",
                    "romperreinos del " + tierLabelSearch
            ));
            return Optional.of(new AliasEntry(display, aliases));
        }
        return Optional.empty();
    }
}
