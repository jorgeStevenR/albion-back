package com.albion.guildbalance.domain.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemSearchNormalizer {

    private static final int MIN_TOKEN_LENGTH = 3;
    private static final int MIN_PREFIX_LENGTH = 2;

    private static final Set<String> STOP_WORDS = Set.of(
            "de", "del", "la", "el", "los", "las", "un", "una", "y", "con", "para", "tier", "t");

    private static final Pattern TIER_IN_QUERY = Pattern.compile("\\b(?:t\\s*)?([4-8])\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENCHANT_IN_QUERY = Pattern.compile(
            "(?:@|\\.)([0-4])\\b|(?:encantamiento|encantado|enchant)\\s*([0-4])",
            Pattern.CASE_INSENSITIVE);

    private static final Map<String, List<String>> SYNONYM_GROUPS = Map.ofEntries(
            Map.entry("casco", List.of("casco", "helmet", "helm", "head")),
            Map.entry("head", List.of("head", "helmet", "helm", "casco")),
            Map.entry("capucha", List.of("capucha", "hood", "cowl", "habito")),
            Map.entry("soldado", List.of("soldado", "soldier")),
            Map.entry("mercenario", List.of("mercenario", "mercenary")),
            Map.entry("guardian", List.of("guardian", "guardián")),
            Map.entry("caballero", List.of("caballero", "knight")),
            Map.entry("armadura", List.of("armadura", "armor", "armour")),
            Map.entry("chaqueta", List.of("chaqueta", "jacket")),
            Map.entry("chaleco", List.of("chaleco", "vest", "jacket", "chaqueta")),
            Map.entry("pechera", List.of("pechera", "armor", "armadura", "plate")),
            Map.entry("placa", List.of("placa", "plate")),
            Map.entry("cuero", List.of("cuero", "leather")),
            Map.entry("tela", List.of("tela", "cloth", "robe")),
            Map.entry("zapatos", List.of("zapatos", "shoes", "boots", "botas")),
            Map.entry("botas", List.of("botas", "boots", "shoes")),
            Map.entry("capa", List.of("capa", "cape")),
            Map.entry("bolsa", List.of("bolsa", "bag", "satchel")),
            Map.entry("bag", List.of("bag", "bolsa", "satchel")),
            Map.entry("montura", List.of("montura", "mount", "horse", "caballo")),
            Map.entry("comida", List.of("comida", "meal", "food")),
            Map.entry("pocion", List.of("pocion", "potion")),
            Map.entry("gran", List.of("gran", "great")),
            Map.entry("martillo", List.of("martillo", "hammer", "great")),
            Map.entry("grande", List.of("grande", "great")),
            Map.entry("relampago", List.of("relampago", "realmbreaker", "romperreinos")),
            Map.entry("romperreinos", List.of("romperreinos", "realmbreaker", "relampago")),
            Map.entry("realmbreaker", List.of("realmbreaker", "relampago", "romperreinos")),
            Map.entry("mazo", List.of("mazo", "hammer", "martillo", "mace")),
            Map.entry("daga", List.of("daga", "dagger")),
            Map.entry("dagas", List.of("dagas", "dagger")),
            Map.entry("falce", List.of("falce", "scythe", "guadana")),
            Map.entry("guadana", List.of("guadana", "scythe", "falce")),
            Map.entry("arco", List.of("arco", "bow")),
            Map.entry("baston", List.of("baston", "staff")),
            Map.entry("escudo", List.of("escudo", "shield")),
            Map.entry("antorcha", List.of("antorcha", "torch")),
            Map.entry("libro", List.of("libro", "book")),
            Map.entry("orbe", List.of("orbe", "orb")),
            Map.entry("espada", List.of("espada", "sword")),
            Map.entry("hacha", List.of("hacha", "axe")),
            Map.entry("tunica", List.of("tunica", "robe", "cloth", "habito")),
            Map.entry("habito", List.of("habito", "robe", "cloth", "tunica")),
            Map.entry("sandalia", List.of("sandalia", "sandals", "shoes")),
            Map.entry("sandalias", List.of("sandalias", "sandals", "shoes")),
            Map.entry("maldicion", List.of("maldicion", "cursed", "cursedstaff")),
            Map.entry("maldiciones", List.of("maldiciones", "cursed", "cursedstaff")),
            Map.entry("arcano", List.of("arcano", "arcane", "arcanestaff")),
            Map.entry("hielo", List.of("hielo", "frost", "froststaff", "glacial")),
            Map.entry("fuego", List.of("fuego", "fire", "firestaff", "infernal")),
            Map.entry("sagrado", List.of("sagrado", "holy", "holystaff", "divine")),
            Map.entry("natural", List.of("natural", "nature", "naturestaff", "wild")),
            Map.entry("naturaleza", List.of("naturaleza", "nature", "naturestaff")),
            Map.entry("cambiaformas", List.of("cambiaformas", "shapeshifter")),
            Map.entry("barra", List.of("barra", "quarterstaff", "baston")),
            Map.entry("barras", List.of("barras", "quarterstaff")),
            Map.entry("punos", List.of("punos", "knuckles", "guanteletes")),
            Map.entry("puno", List.of("puno", "knuckles")),
            Map.entry("espadas", List.of("espadas", "sword", "claymore")),
            Map.entry("hachas", List.of("hachas", "axe")),
            Map.entry("mazas", List.of("mazas", "mace", "hammer")),
            Map.entry("martillos", List.of("martillos", "hammer", "mace")),
            Map.entry("arcos", List.of("arcos", "bow", "warbow", "longbow")),
            Map.entry("ballestas", List.of("ballestas", "crossbow")),
            Map.entry("lanzas", List.of("lanzas", "spear", "glaive", "halberd")),
            Map.entry("ballesta", List.of("ballesta", "crossbow")),
            Map.entry("cultista", List.of("cultista", "cultist", "sectario")),
            Map.entry("sectario", List.of("sectario", "cultist")),
            Map.entry("morgana", List.of("morgana")),
            Map.entry("keeper", List.of("keeper", "druida")),
            Map.entry("infernal", List.of("infernal", "demon", "demonio")),
            Map.entry("demonio", List.of("demonio", "demon", "infernal"))
    );

    private ItemSearchNormalizer() {
    }

    public static ItemSearchQuery parse(String rawQuery, Integer tier, Integer enchantment, Integer quality) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new ItemSearchQuery(List.of(), null, tier, enchantment, quality);
        }

        String working = normalize(rawQuery);
        Integer parsedTier = tier;
        Integer parsedEnchant = enchantment;

        Matcher tierMatcher = TIER_IN_QUERY.matcher(working);
        if (parsedTier == null && tierMatcher.find()) {
            parsedTier = Integer.parseInt(tierMatcher.group(1));
            working = tierMatcher.replaceAll(" ").trim();
        }

        Matcher enchantMatcher = ENCHANT_IN_QUERY.matcher(working);
        if (parsedEnchant == null && enchantMatcher.find()) {
            String group = enchantMatcher.group(1) != null ? enchantMatcher.group(1) : enchantMatcher.group(2);
            parsedEnchant = Integer.parseInt(group);
            working = enchantMatcher.replaceAll(" ").trim();
        }

        String[] rawTokens = working.split("\\s+");
        List<String> meaningfulTokens = new ArrayList<>();
        for (String token : rawTokens) {
            String cleaned = token.replaceAll("[^a-z0-9]", "");
            if (cleaned.isBlank() || STOP_WORDS.contains(cleaned) || cleaned.length() < MIN_TOKEN_LENGTH) {
                continue;
            }
            meaningfulTokens.add(cleaned);
        }

        List<List<String>> termGroups = new ArrayList<>(meaningfulTokens.stream()
                .map(ItemSearchNormalizer::synonymsFor)
                .toList());

        String trailingPrefix = trailingPrefix(rawTokens, meaningfulTokens);
        if (trailingPrefix != null) {
            List<String> prefixTerms = expandPrefix(trailingPrefix);
            if (!prefixTerms.isEmpty()) {
                termGroups.add(prefixTerms);
            }
        }

        String phrase = meaningfulTokens.isEmpty() ? null : String.join(" ", meaningfulTokens);

        return new ItemSearchQuery(termGroups, phrase, parsedTier, parsedEnchant, quality);
    }

    private static String trailingPrefix(String[] rawTokens, List<String> meaningfulTokens) {
        if (rawTokens.length == 0 || rawTokens.length == meaningfulTokens.size()) {
            return null;
        }
        String last = rawTokens[rawTokens.length - 1].replaceAll("[^a-z0-9]", "");
        if (last.length() < MIN_PREFIX_LENGTH || STOP_WORDS.contains(last)) {
            return null;
        }
        return last;
    }

    static List<String> expandPrefix(String prefix) {
        Set<String> terms = new LinkedHashSet<>();
        if (prefix == null || prefix.length() < MIN_PREFIX_LENGTH) {
            return List.of();
        }
        String normalized = normalize(prefix);
        for (Map.Entry<String, List<String>> entry : SYNONYM_GROUPS.entrySet()) {
            if (entry.getKey().startsWith(normalized)) {
                terms.add(entry.getKey());
                terms.addAll(entry.getValue());
            }
            for (String synonym : entry.getValue()) {
                if (synonym.startsWith(normalized)) {
                    terms.add(synonym);
                    terms.add(entry.getKey());
                }
            }
        }
        return new ArrayList<>(terms);
    }

    private static List<String> synonymsFor(String token) {
        Set<String> group = new LinkedHashSet<>();
        group.add(token);
        List<String> mapped = SYNONYM_GROUPS.get(token);
        if (mapped != null) {
            group.addAll(mapped);
        }
        group.addAll(expandPrefix(token));
        return new ArrayList<>(group);
    }

    private static String normalize(String input) {
        return ItemSearchTextBuilder.normalize(input);
    }
}
