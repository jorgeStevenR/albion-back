package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.LoginRequest;
import com.albion.guildbalance.application.dto.response.AuthResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
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

        String token = jwtService.generateToken(player);

        return AuthResponse.builder()
                .token(token)
                .playerId(player.getId())
                .albionName(player.getAlbionName())
                .role(player.getRole())
                .build();
    }
}
