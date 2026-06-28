package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.AvalonRunRequest;
import com.albion.guildbalance.application.dto.request.LootItemRequest;
import com.albion.guildbalance.application.dto.request.ParticipantRequest;
import com.albion.guildbalance.application.dto.response.AvalonRunResponse;
import com.albion.guildbalance.application.dto.response.DistributionCalculationResponse;
import com.albion.guildbalance.application.service.AvalonRunService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avalons")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Avalon Runs", description = "Avalonian run management")
public class AvalonRunController {

    private final AvalonRunService avalonRunService;

    @GetMapping
    @Operation(summary = "List all avalon runs")
    public ResponseEntity<ApiResponse<List<AvalonRunResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(avalonRunService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get avalon run by ID")
    public ResponseEntity<ApiResponse<AvalonRunResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(avalonRunService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new avalon run")
    public ResponseEntity<ApiResponse<AvalonRunResponse>> create(@Valid @RequestBody AvalonRunRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Avalon run created", avalonRunService.create(request)));
    }

    @PostMapping("/{id}/participants")
    @Operation(summary = "Add participant to avalon run")
    public ResponseEntity<ApiResponse<AvalonRunResponse>> addParticipant(
            @PathVariable Long id,
            @Valid @RequestBody ParticipantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Participant added", avalonRunService.addParticipant(id, request)));
    }

    @PostMapping("/{id}/loot")
    @Operation(summary = "Add loot to avalon run")
    public ResponseEntity<ApiResponse<AvalonRunResponse>> addLoot(
            @PathVariable Long id,
            @Valid @RequestBody LootItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Loot added", avalonRunService.addLoot(id, request)));
    }

    @PostMapping("/{id}/calculate")
    @Operation(summary = "Calculate distribution and finish avalon run")
    public ResponseEntity<ApiResponse<DistributionCalculationResponse>> calculate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reparto calculado — avaloniana terminada", avalonRunService.calculateAndFinish(id)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close avalon run after all loot is sold")
    public ResponseEntity<ApiResponse<AvalonRunResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Avaloniana cerrada", avalonRunService.closeAvalon(id)));
    }
}
