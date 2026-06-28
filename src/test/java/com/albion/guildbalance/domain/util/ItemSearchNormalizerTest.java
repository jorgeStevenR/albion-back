package com.albion.guildbalance.domain.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSearchNormalizerTest {

    @Test
    void martilloRelampagoExpandsRealmbreakerSynonyms() {
        ItemSearchQuery query = ItemSearchNormalizer.parse("martillo relámpago", null, null, null);

        assertThat(query.termGroups()).hasSize(2);
        assertThat(query.termGroups().get(0)).contains("martillo");
        assertThat(query.termGroups().get(1)).contains("relampago", "realmbreaker", "romperreinos");
        assertThat(query.phrase()).isEqualTo("martillo relampago");
    }

    @Test
    void shortPartialTokenIsIgnoredWhileTyping() {
        ItemSearchQuery query = ItemSearchNormalizer.parse("martillo re", null, null, null);

        assertThat(query.termGroups()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(query.termGroups().get(0)).contains("martillo", "hammer");
        assertThat(query.termGroups().stream().flatMap(List::stream))
                .anyMatch(term -> term.contains("realmbreaker") || term.contains("relampago"));
        assertThat(query.phrase()).isEqualTo("martillo");
    }

    @Test
    void partialRelPrefixMatchesRelampago() {
        ItemSearchQuery query = ItemSearchNormalizer.parse("martillo rel", null, null, null);

        assertThat(query.termGroups()).hasSize(2);
        assertThat(query.termGroups().get(1)).contains("relampago");
    }
}
