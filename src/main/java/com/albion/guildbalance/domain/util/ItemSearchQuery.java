package com.albion.guildbalance.domain.util;

import java.util.List;

public record ItemSearchQuery(
        List<List<String>> termGroups,
        String phrase,
        Integer tier,
        Integer enchantment,
        Integer quality
) {
    public boolean hasNameFilter() {
        return (phrase != null && !phrase.isBlank())
                || (termGroups != null && !termGroups.isEmpty());
    }

    public boolean hasTermGroups() {
        return termGroups != null && !termGroups.isEmpty();
    }
}
