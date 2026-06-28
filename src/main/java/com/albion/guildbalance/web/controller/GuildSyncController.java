package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.SyncGuildRequest;
import com.albion.guildbalance.application.dto.response.GuildInfoResponse;
import com.albion.guildbalance.application.dto.response.GuildPlayerDetailResponse;
import com.albion.guildbalance.application.dto.response.GuildPlayerResponse;
import com.albion.guildbalance.application.dto.response.SyncGuildResponse;
import com.albion.guildbalance.application.service.GuildPlayerService;
import com.albion.guildbalance.application.service.GuildSyncService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guild")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Guild Sync", description = "Albion Online guild synchronization")
public class GuildSyncController {

    private final GuildSyncService guildSyncService;
    private final GuildPlayerService guildPlayerService;

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Synchronize configured guild members from Albion Online API")
    public ResponseEntity<ApiResponse<SyncGuildResponse>> syncGuild(
            @RequestBody(required = false) SyncGuildRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Guild synchronized", guildSyncService.syncConfiguredGuild()));
    }

    @GetMapping("/info")
    @Operation(summary = "Configured guild name")
    public ResponseEntity<ApiResponse<GuildInfoResponse>> guildInfo() {
        return ResponseEntity.ok(ApiResponse.success(guildSyncService.getGuildInfo()));
    }

    @GetMapping("/players")
    @PreAuthorize("hasAnyRole('ADMIN', 'CALLER', 'OFFICER', 'PLAYER')")
    @Operation(summary = "List all synchronized guild players")
    public ResponseEntity<ApiResponse<List<GuildPlayerResponse>>> findAllPlayers() {
        return ResponseEntity.ok(ApiResponse.success(guildPlayerService.findAllSynced()));
    }

    @GetMapping("/players/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CALLER', 'OFFICER', 'PLAYER')")
    @Operation(summary = "Get synchronized player detail with balance and history")
    public ResponseEntity<ApiResponse<GuildPlayerDetailResponse>> findPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(guildPlayerService.findById(id)));
    }
}
