package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.LoginRequest;
import com.albion.guildbalance.application.dto.response.AuthResponse;
import com.albion.guildbalance.application.dto.response.GuildInfoResponse;
import com.albion.guildbalance.application.service.AuthService;
import com.albion.guildbalance.infrastructure.config.GuildProperties;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final GuildProperties guildProperties;

    @PostMapping("/login")
    @Operation(summary = "Login and obtain JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @GetMapping("/guild-info")
    @Operation(summary = "Public guild name for login screen")
    public ResponseEntity<ApiResponse<GuildInfoResponse>> guildInfo() {
        return ResponseEntity.ok(ApiResponse.success(GuildInfoResponse.builder()
                .name(guildProperties.getName())
                .build()));
    }
}
