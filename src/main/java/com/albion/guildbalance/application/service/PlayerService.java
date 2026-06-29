package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.PlayerRequest;
import com.albion.guildbalance.application.dto.request.PlayerUpdateRequest;
import com.albion.guildbalance.application.dto.response.PlayerResponse;
import com.albion.guildbalance.application.exception.DuplicateResourceException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepositoryPort playerRepository;
    private final WalletRepositoryPort walletRepository;
    private final EntityMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        log.debug("Fetching all players");
        return mapper.toPlayerResponseList(playerRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PlayerResponse findById(Long id) {
        return mapper.toPlayerResponse(getPlayerOrThrow(id));
    }

    @Transactional
    public PlayerResponse create(PlayerRequest request) {
        log.info("Creating player: {}", request.getAlbionName());
        if (playerRepository.existsByAlbionName(request.getAlbionName())) {
            throw new DuplicateResourceException("Player with albion name already exists: " + request.getAlbionName());
        }

        Player player = Player.builder()
                .albionName(request.getAlbionName())
                .discordName(request.getDiscordName())
                .role(request.getRole())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .mustChangePassword(true)
                .build();

        Player saved = playerRepository.save(player);

        walletRepository.save(Wallet.builder()
                .player(saved)
                .balance(BigDecimal.ZERO)
                .build());

        return mapper.toPlayerResponse(saved);
    }

    @Transactional
    public PlayerResponse update(Long id, PlayerUpdateRequest request) {
        log.info("Updating player id: {}", id);
        Player player = getPlayerOrThrow(id);

        if (!player.getAlbionName().equals(request.getAlbionName())
                && playerRepository.existsByAlbionName(request.getAlbionName())) {
            throw new DuplicateResourceException("Player with albion name already exists: " + request.getAlbionName());
        }

        player.setAlbionName(request.getAlbionName());
        player.setDiscordName(request.getDiscordName());
        player.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            player.setPassword(passwordEncoder.encode(request.getPassword()));
            player.setMustChangePassword(true);
        }

        return mapper.toPlayerResponse(playerRepository.save(player));
    }

    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating player id: {}", id);
        Player player = getPlayerOrThrow(id);
        player.setActive(false);
        playerRepository.save(player);
    }

    private Player getPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
    }
}
