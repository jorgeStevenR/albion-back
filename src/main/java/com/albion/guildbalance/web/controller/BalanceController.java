package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.response.BalanceResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.service.BalanceService;
import com.albion.guildbalance.domain.enums.PlayerRole;
import com.albion.guildbalance.web.dto.ApiResponse;
import com.albion.guildbalance.web.security.PlayerPrincipal;
import com.albion.guildbalance.web.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Balance", description = "Player balance and distribution history")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/player/{id}")
    @Operation(summary = "Get player balance and distribution history")
    public ResponseEntity<ApiResponse<BalanceResponse>> getPlayerBalance(@PathVariable Long id) {
        PlayerPrincipal current = SecurityUtils.getCurrentPlayer();

        if (current.getRole() == PlayerRole.PLAYER && !current.getPlayerId().equals(id)) {
            throw new BusinessException("Players can only view their own balance");
        }

        return ResponseEntity.ok(ApiResponse.success(balanceService.getPlayerBalance(id)));
    }
}
