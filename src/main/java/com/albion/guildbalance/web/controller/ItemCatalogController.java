package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.response.AlbionItemResponse;
import com.albion.guildbalance.application.dto.response.ItemCatalogStatusResponse;
import com.albion.guildbalance.application.service.AlbionItemCatalogService;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Albion Items", description = "Item catalog search for build templates")
public class ItemCatalogController {

    private final AlbionItemCatalogService itemCatalogService;

    @GetMapping("/catalog-status")
    @Operation(summary = "Item catalog load status")
    public ResponseEntity<ApiResponse<ItemCatalogStatusResponse>> catalogStatus() {
        return ResponseEntity.ok(ApiResponse.success(itemCatalogService.getCatalogStatus()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Albion items with filters")
    public ResponseEntity<ApiResponse<List<AlbionItemResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EquipmentSlot slot,
            @RequestParam(required = false) Integer tier,
            @RequestParam(required = false) Integer enchantment,
            @RequestParam(required = false) Integer quality,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                itemCatalogService.search(q, slot, tier, enchantment, quality, limit)));
    }

    @GetMapping("/filters")
    @Operation(summary = "Filter options for item search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> filters() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "tiers", List.of(4, 5, 6, 7, 8),
                "enchantments", List.of(
                        Map.of("value", 0, "label", "Sin encantar (.0)"),
                        Map.of("value", 1, "label", "Encantamiento .1"),
                        Map.of("value", 2, "label", "Encantamiento .2"),
                        Map.of("value", 3, "label", "Encantamiento .3"),
                        Map.of("value", 4, "label", "Encantamiento .4")
                ),
                "qualities", List.of(
                        Map.of("value", 1, "label", "Normal (plantilla)"),
                        Map.of("value", 2, "label", "Buena"),
                        Map.of("value", 3, "label", "Sobresaliente"),
                        Map.of("value", 4, "label", "Excelente"),
                        Map.of("value", 5, "label", "Obra maestra")
                )
        )));
    }

    @GetMapping("/slots")
    @Operation(summary = "List available equipment slots")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> slots() {
        List<Map<String, String>> slots = java.util.Arrays.stream(EquipmentSlot.values())
                .map(s -> Map.of("value", s.name(), "label", formatSlotLabel(s)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Re-sync item catalog from ao-bin-dumps")
    public ResponseEntity<ApiResponse<Integer>> sync() {
        return ResponseEntity.ok(ApiResponse.success(
                "Catalog synced", itemCatalogService.syncCatalog()));
    }

    private String formatSlotLabel(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> "Arma principal";
            case OFFHAND -> "Mano secundaria";
            case HEAD -> "Casco / Capucha";
            case ARMOR -> "Armadura / Chaqueta";
            case SHOES -> "Zapatos";
            case CAPE -> "Capa";
            case BAG -> "Bolsa";
            case MOUNT -> "Montura";
            case FOOD -> "Comida";
            case POTION -> "Poción";
        };
    }
}
