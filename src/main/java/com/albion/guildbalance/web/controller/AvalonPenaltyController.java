package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.AssignDelegateRequest;
import com.albion.guildbalance.application.dto.request.ManualPenaltyRequest;
import com.albion.guildbalance.application.dto.request.MassFineRequest;
import com.albion.guildbalance.application.dto.request.NoShowPenaltyRequest;
import com.albion.guildbalance.application.dto.response.AvalonDelegateResponse;
import com.albion.guildbalance.application.dto.response.AvalonPenaltyResponse;
import com.albion.guildbalance.application.service.AvalonPenaltyService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avalons/{avalonId}/penalties")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Avalon Penalties", description = "Fines and rewards when closing avalon runs")
public class AvalonPenaltyController {

    private final AvalonPenaltyService penaltyService;

    @GetMapping
    @Operation(summary = "List penalties for an avalon run")
    public ResponseEntity<ApiResponse<List<AvalonPenaltyResponse>>> list(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(penaltyService.listByAvalon(avalonId)));
    }

    @GetMapping("/can-manage")
    @Operation(summary = "Check if current user can manage penalties")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canManage(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("canManage", penaltyService.canManagePenalties(avalonId))));
    }

    @PostMapping("/no-show")
    @Operation(summary = "Fine no-show player and reward replacement")
    public ResponseEntity<ApiResponse<List<AvalonPenaltyResponse>>> noShow(
            @PathVariable Long avalonId,
            @Valid @RequestBody NoShowPenaltyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Multa y recompensa aplicadas", penaltyService.applyNoShow(avalonId, request)));
    }

    @PostMapping("/mass")
    @Operation(summary = "Fine all registered players")
    public ResponseEntity<ApiResponse<List<AvalonPenaltyResponse>>> massFine(
            @PathVariable Long avalonId,
            @Valid @RequestBody MassFineRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Multa masiva aplicada", penaltyService.applyMassFine(avalonId, request)));
    }

    @PostMapping("/manual")
    @Operation(summary = "Apply manual fine or reward to a player")
    public ResponseEntity<ApiResponse<AvalonPenaltyResponse>> manual(
            @PathVariable Long avalonId,
            @Valid @RequestBody ManualPenaltyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Ajuste aplicado", penaltyService.applyManual(avalonId, request)));
    }

    @GetMapping("/delegates")
    @Operation(summary = "List penalty delegates for this avalon")
    public ResponseEntity<ApiResponse<List<AvalonDelegateResponse>>> listDelegates(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(penaltyService.listDelegates(avalonId)));
    }

    @PostMapping("/delegates")
    @Operation(summary = "Assign a delegate who can manage penalties")
    public ResponseEntity<ApiResponse<AvalonDelegateResponse>> assignDelegate(
            @PathVariable Long avalonId,
            @Valid @RequestBody AssignDelegateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delegado asignado", penaltyService.assignDelegate(avalonId, request)));
    }

    @DeleteMapping("/delegates/{playerId}")
    @Operation(summary = "Remove a penalty delegate")
    public ResponseEntity<ApiResponse<Void>> removeDelegate(
            @PathVariable Long avalonId,
            @PathVariable Long playerId) {
        penaltyService.removeDelegate(avalonId, playerId);
        return ResponseEntity.ok(ApiResponse.success("Delegado eliminado", null));
    }
}
