package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.ChangePasswordRequest;
import com.albion.guildbalance.application.dto.request.LoginRequest;
import com.albion.guildbalance.application.dto.response.AuthResponse;
import com.albion.guildbalance.application.dto.response.UserProfileResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlayerRepositoryPort playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for player: {}", request.getAlbionName());
        Player player = playerRepository.findByAlbionName(request.getAlbionName())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!player.isActive()) {
            throw new BusinessException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), player.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        return buildAuthResponse(player);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile() {
        Player player = getCurrentPlayerEntity();
        return toProfileResponse(player);
    }

    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        Player player = getCurrentPlayerEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), player.getPassword())) {
            throw new BusinessException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), player.getPassword())) {
            throw new BusinessException("La nueva contraseña debe ser diferente a la actual");
        }

        player.setPassword(passwordEncoder.encode(request.getNewPassword()));
        player.setMustChangePassword(false);
        Player saved = playerRepository.save(player);

        log.info("Password changed for player id: {}", saved.getId());
        return buildAuthResponse(saved);
    }

    private Player getCurrentPlayerEntity() {
        Long playerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private AuthResponse buildAuthResponse(Player player) {
        String token = jwtService.generateToken(player);

        return AuthResponse.builder()
                .token(token)
                .playerId(player.getId())
                .albionName(player.getAlbionName())
                .role(player.getRole())
                .mustChangePassword(player.isMustChangePassword())
                .build();
    }

    private UserProfileResponse toProfileResponse(Player player) {
        return UserProfileResponse.builder()
                .playerId(player.getId())
                .albionName(player.getAlbionName())
                .discordName(player.getDiscordName())
                .rank(player.getRank())
                .role(player.getRole())
                .mustChangePassword(player.isMustChangePassword())
                .build();
    }
}
