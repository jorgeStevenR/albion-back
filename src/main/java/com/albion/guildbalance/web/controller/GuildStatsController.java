package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.response.GuildStatsResponse;
import com.albion.guildbalance.application.dto.response.GuildTransactionResponse;
import com.albion.guildbalance.application.service.GuildStatsService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guild")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Guild Stats", description = "Guild-wide statistics and transactions")
public class GuildStatsController {

    private final GuildStatsService guildStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get guild dashboard statistics")
    public ResponseEntity<ApiResponse<GuildStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(guildStatsService.getStats()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get guild transaction history")
    public ResponseEntity<ApiResponse<List<GuildTransactionResponse>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.success(guildStatsService.getTransactions()));
    }
}
