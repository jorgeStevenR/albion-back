package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.response.AlbionItemResponse;
import com.albion.guildbalance.application.dto.response.ItemCatalogStatusResponse;
import com.albion.guildbalance.domain.entity.AlbionItem;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import com.albion.guildbalance.domain.util.BuildItemFilter;
import com.albion.guildbalance.domain.util.ItemDisplayNames;
import com.albion.guildbalance.domain.util.ItemGameAliases;
import com.albion.guildbalance.domain.util.ItemIdParser;
import com.albion.guildbalance.domain.util.ItemSearchNormalizer;
import com.albion.guildbalance.domain.util.ItemSearchQuery;
import com.albion.guildbalance.domain.util.ItemSearchTextBuilder;
import com.albion.guildbalance.domain.util.ItemSlotClassifier;
import com.albion.guildbalance.infrastructure.persistence.repository.AlbionItemJdbcRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.AlbionItemJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbionItemCatalogService {

    private static final String ITEMS_JSON_URL =
            "https://raw.githubusercontent.com/ao-data/ao-bin-dumps/master/formatted/items.json";
    /** Real catalog after gear filter is ~7.5k items — do not force re-sync above this. */
    private static final int MIN_EXPECTED_ITEMS = 5000;
    private static final int MIN_SEARCHABLE_ITEMS = 500;

    private final AlbionItemJpaRepository repository;
    private final AlbionItemCatalogSyncHelper syncHelper;
    private final ObjectMapper objectMapper;
    private final Object syncLock = new Object();
    private final AtomicBoolean backgroundSyncScheduled = new AtomicBoolean(false);
    private volatile boolean syncInProgress;

    @Value("${albion.items.render-base-url:https://render.albiononline.com/v1/item}")
    private String renderBaseUrl;

    public List<AlbionItemResponse> search(
            String query,
            EquipmentSlot slot,
            Integer tier,
            Integer enchantment,
            Integer quality,
            int limit) {
        long count = repository.count();
        if (count == 0 && syncInProgress) {
            log.debug("Item catalog sync in progress — search skipped");
            return List.of();
        }
        if (count == 0) {
            scheduleBackgroundSyncIfNeeded();
            log.warn("Item catalog empty — search skipped (background sync scheduled)");
            return List.of();
        }

        ItemSearchQuery parsed = ItemSearchNormalizer.parse(query, tier, enchantment, quality);
        int capped = Math.min(Math.max(limit, 1), maxLimit(parsed, slot));

        if (!parsed.hasNameFilter() && slot == null && parsed.tier() == null
                && parsed.enchantment() == null) {
            return List.of();
        }

        Specification<AlbionItem> spec = buildSpec(parsed, slot);
        Map<String, AlbionItem> deduped = new LinkedHashMap<>();
        int fetchLimit = parsed.enchantment() == null ? capped * 5 : capped;
        for (AlbionItem item : repository.findAll(spec, PageRequest.of(0, fetchLimit))) {
            if (parsed.enchantment() == null && item.getEnchantment() > 0) {
                continue;
            }
            String key = item.getUniqueName().replaceAll("@[0-4]$", "");
            deduped.putIfAbsent(key, item);
            if (deduped.size() >= capped) {
                break;
            }
        }
        return deduped.values().stream()
                .map(this::toResponse)
                .toList();
    }

    private int maxLimit(ItemSearchQuery parsed, EquipmentSlot slot) {
        if (slot != null && !parsed.hasNameFilter()) {
            return 150;
        }
        return 50;
    }

    public ItemCatalogStatusResponse getCatalogStatus() {
        long count = repository.count();
        if (count == 0) {
            scheduleBackgroundSyncIfNeeded();
        }
        return ItemCatalogStatusResponse.builder()
                .itemCount(count)
                .ready(count >= MIN_SEARCHABLE_ITEMS)
                .syncInProgress(syncInProgress)
                .build();
    }

    public void scheduleBackgroundSyncIfNeeded() {
        if (!backgroundSyncScheduled.compareAndSet(false, true)) {
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                log.info("Background Albion item catalog sync started");
                syncCatalogIfEmpty();
            } catch (Exception ex) {
                log.error("Background Albion item catalog sync failed", ex);
                backgroundSyncScheduled.set(false);
            }
        });
    }

    public void ensureCatalogFresh() {
        if (!needsResync()) {
            return;
        }
        syncCatalog();
    }

    private boolean needsResync() {
        long count = repository.count();
        if (count < MIN_SEARCHABLE_ITEMS) {
            return true;
        }
        return !repository.existsById("T8_2H_HAMMER");
    }

    public int syncCatalogIfEmpty() {
        long count = repository.count();
        if (!needsResync()) {
            log.info("Albion item catalog already loaded ({} items)", count);
            return (int) count;
        }
        if (count > 0) {
            log.warn("Albion item catalog outdated or incomplete ({} items) — re-syncing", count);
        }
        return syncCatalog();
    }

    public int syncCatalog() {
        synchronized (syncLock) {
            if (syncInProgress) {
                log.info("Catalog sync already in progress");
                return (int) repository.count();
            }
            syncInProgress = true;
            try {
                return doSyncCatalog();
            } finally {
                syncInProgress = false;
            }
        }
    }

    private int doSyncCatalog() {
        log.info("Syncing Albion item catalog from ao-bin-dumps (JSON + ES names)...");

        String body = RestClient.create()
                .get()
                .uri(ITEMS_JSON_URL)
                .retrieve()
                .body(String.class);

        if (body == null || body.isBlank()) {
            throw new IllegalStateException("Failed to download Albion items catalog");
        }

        List<AlbionItem> items = parseCatalogJson(body);
        if (items.isEmpty()) {
            throw new IllegalStateException("Parsed Albion items catalog is empty");
        }

        syncHelper.clearAll();
        int imported = syncHelper.saveAllInBatches(items);
        log.info("Albion item catalog synced: {} build-related items", imported);
        return imported;
    }

    private List<AlbionItem> parseCatalogJson(String body) {
        List<AlbionItem> items = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.isArray()) {
                throw new IllegalStateException("Unexpected items.json format");
            }
            for (JsonNode node : root) {
                String uniqueName = node.path("UniqueName").asText(null);
                if (uniqueName == null || uniqueName.isBlank()) {
                    continue;
                }
                if (!BuildItemFilter.isEquippableGear(uniqueName)) {
                    continue;
                }
                EquipmentSlot equipmentSlot = ItemSlotClassifier.classify(uniqueName);
                if (equipmentSlot == null) {
                    continue;
                }
                String displayEn = node.path("LocalizedNames").path("EN-US").asText(uniqueName);
                String displayEs = node.path("LocalizedNames").path("ES-ES").asText(displayEn);
                var gameAlias = ItemGameAliases.forUniqueName(uniqueName);
                if (gameAlias.isPresent()) {
                    displayEs = gameAlias.get().displayNameEs();
                }

                items.add(AlbionItem.builder()
                        .uniqueName(uniqueName)
                        .displayName(displayEn)
                        .displayNameEs(displayEs)
                        .equipmentSlot(equipmentSlot)
                        .tier(ItemIdParser.parseTier(uniqueName))
                        .enchantment(ItemIdParser.parseEnchantment(uniqueName))
                        .quality(1)
                        .searchText(ItemSearchTextBuilder.build(displayEn, displayEs, uniqueName))
                        .build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Albion items catalog", ex);
        }
        return items;
    }

    private Specification<AlbionItem> buildSpec(ItemSearchQuery parsed, EquipmentSlot slot) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (slot != null) {
                predicates.add(cb.equal(root.get("equipmentSlot"), slot));
            }
            if (parsed.tier() != null) {
                predicates.add(cb.equal(root.get("tier"), parsed.tier()));
            }
            if (parsed.enchantment() != null) {
                predicates.add(cb.equal(root.get("enchantment"), parsed.enchantment()));
            }

            if (parsed.hasNameFilter()) {
                List<Predicate> namePredicates = new ArrayList<>();

                if (parsed.phrase() != null && !parsed.phrase().isBlank()) {
                    namePredicates.add(cb.like(
                            root.get("searchText"),
                            "%" + parsed.phrase() + "%"));
                }

                if (parsed.hasTermGroups()) {
                    List<Predicate> groupAnd = new ArrayList<>();
                    for (List<String> group : parsed.termGroups()) {
                        List<Predicate> orInGroup = new ArrayList<>();
                        for (String term : group) {
                            orInGroup.add(cb.like(
                                    root.get("searchText"),
                                    "%" + ItemSearchTextBuilder.normalize(term) + "%"));
                        }
                        groupAnd.add(cb.or(orInGroup.toArray(Predicate[]::new)));
                    }
                    namePredicates.add(cb.and(groupAnd.toArray(Predicate[]::new)));
                }

                predicates.add(cb.or(namePredicates.toArray(Predicate[]::new)));
            }

            query.orderBy(
                    cb.desc(root.get("tier")),
                    cb.asc(root.get("enchantment")),
                    cb.asc(root.get("displayNameEs")),
                    cb.asc(root.get("displayName"))
            );
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AlbionItemResponse toResponse(AlbionItem item) {
        String displayEs = ItemDisplayNames.spanishName(item);
        return AlbionItemResponse.builder()
                .uniqueName(item.getUniqueName())
                .displayName(item.getDisplayName())
                .displayNameEs(displayEs)
                .label(ItemDisplayNames.label(item))
                .equipmentSlot(item.getEquipmentSlot())
                .tier(item.getTier())
                .enchantment(item.getEnchantment())
                .quality(item.getQuality())
                .iconUrl(renderBaseUrl + "/" + item.getUniqueName() + ".png")
                .build();
    }
}
