package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.AdminManualPenaltyRequest;
import com.albion.guildbalance.application.dto.request.ReviewAppealRequest;
import com.albion.guildbalance.application.dto.request.SubmitAppealRequest;
import com.albion.guildbalance.application.dto.response.AvalonPenaltyResponse;
import com.albion.guildbalance.application.dto.response.PenaltyAppealResponse;
import com.albion.guildbalance.application.service.AvalonPenaltyService;
import com.albion.guildbalance.application.service.PenaltyAppealService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Penalty Appeals", description = "Appeal fines and review appeals")
public class PenaltyAppealController {

    private final PenaltyAppealService appealService;
    private final AvalonPenaltyService penaltyService;

    @GetMapping("/api/penalties/all")
    @Operation(summary = "List all guild penalties (admin)")
    public ResponseEntity<ApiResponse<List<AvalonPenaltyResponse>>> allPenalties() {
        return ResponseEntity.ok(ApiResponse.success(penaltyService.listAllPenalties()));
    }

    @PostMapping("/api/penalties/admin/manual")
    @Operation(summary = "Create a manual penalty (admin)")
    public ResponseEntity<ApiResponse<AvalonPenaltyResponse>> adminManual(
            @Valid @RequestBody AdminManualPenaltyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Multa creada", penaltyService.applyAdminManual(request)));
    }

    @GetMapping("/api/penalties/mine")
    @Operation(summary = "List my penalties")
    public ResponseEntity<ApiResponse<List<AvalonPenaltyResponse>>> myPenalties() {
        return ResponseEntity.ok(ApiResponse.success(penaltyService.listMyPenalties()));
    }

    @PostMapping("/api/penalties/{penaltyId}/appeal")
    @Operation(summary = "Submit an appeal for a penalty")
    public ResponseEntity<ApiResponse<PenaltyAppealResponse>> submitAppeal(
            @PathVariable Long penaltyId,
            @Valid @RequestBody SubmitAppealRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Apelación enviada", appealService.submitAppeal(penaltyId, request)));
    }

    @GetMapping("/api/penalties/appeals/mine")
    @Operation(summary = "List my appeals")
    public ResponseEntity<ApiResponse<List<PenaltyAppealResponse>>> myAppeals() {
        return ResponseEntity.ok(ApiResponse.success(appealService.listMyAppeals()));
    }

    @GetMapping("/api/penalties/appeals/all")
    @Operation(summary = "List all appeals (admin)")
    public ResponseEntity<ApiResponse<List<PenaltyAppealResponse>>> allAppeals() {
        return ResponseEntity.ok(ApiResponse.success(appealService.listAllAppeals()));
    }

    @GetMapping("/api/penalties/appeals/pending")
    @Operation(summary = "List pending appeals (officers)")
    public ResponseEntity<ApiResponse<List<PenaltyAppealResponse>>> pendingAppeals() {
        return ResponseEntity.ok(ApiResponse.success(appealService.listPendingAppeals()));
    }

    @PostMapping("/api/penalties/appeals/{appealId}/review")
    @Operation(summary = "Review an appeal (approve or reject)")
    public ResponseEntity<ApiResponse<PenaltyAppealResponse>> reviewAppeal(
            @PathVariable Long appealId,
            @Valid @RequestBody ReviewAppealRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Apelación revisada", appealService.reviewAppeal(appealId, request)));
    }
}
