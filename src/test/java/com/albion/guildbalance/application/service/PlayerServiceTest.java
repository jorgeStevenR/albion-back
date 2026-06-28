package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.PlayerRequest;
import com.albion.guildbalance.application.exception.DuplicateResourceException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.PlayerRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepositoryPort playerRepository;

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private EntityMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PlayerService playerService;

    @Test
    @DisplayName("Create player throws when albion name already exists")
    void create_duplicateName_throwsException() {
        PlayerRequest request = PlayerRequest.builder()
                .albionName("ExistingPlayer")
                .discordName("discord")
                .role(PlayerRole.PLAYER)
                .password("password")
                .build();

        when(playerRepository.existsByAlbionName("ExistingPlayer")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> playerService.create(request));
        verify(playerRepository, never()).save(any());
    }
}
