package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.PlayerRequest;
import com.albion.guildbalance.application.dto.request.PlayerUpdateRequest;
import com.albion.guildbalance.application.dto.response.PlayerResponse;
import com.albion.guildbalance.application.service.PlayerService;
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
@RequestMapping("/api/players")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Players", description = "Player management (ADMIN only)")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "List all players")
    public ResponseEntity<ApiResponse<List<PlayerResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(playerService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    public ResponseEntity<ApiResponse<PlayerResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(playerService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new player")
    public ResponseEntity<ApiResponse<PlayerResponse>> create(@Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Player created", playerService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a player")
    public ResponseEntity<ApiResponse<PlayerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Player updated", playerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a player")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        playerService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Player deactivated", null));
    }
}
