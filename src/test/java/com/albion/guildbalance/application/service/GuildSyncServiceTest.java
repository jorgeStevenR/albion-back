package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.albion.AlbionGuildResponse;
import com.albion.guildbalance.application.dto.albion.AlbionPlayerResponse;
import com.albion.guildbalance.application.dto.request.SyncGuildRequest;
import com.albion.guildbalance.application.dto.response.SyncGuildResponse;
import com.albion.guildbalance.application.exception.GuildNotFoundException;
import com.albion.guildbalance.application.port.AlbionApiPort;
import com.albion.guildbalance.application.port.GuildRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Guild;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.PlayerRole;
import com.albion.guildbalance.infrastructure.config.GuildProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GuildSyncServiceTest {

    private static final String GUILD_NAME = "II TEMPUS FUGIT II";
    private static final String DEFAULT_PASSWORD = "tempus123";

    @Mock
    private AlbionApiPort albionApi;

    @Mock
    private GuildRepositoryPort guildRepository;

    @Mock
    private PlayerRepositoryPort playerRepository;

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GuildProperties guildProperties;

    @InjectMocks
    private GuildSyncService guildSyncService;

    @BeforeEach
    void setUp() {
        when(guildProperties.getName()).thenReturn(GUILD_NAME);
        when(guildProperties.getDefaultMemberPassword()).thenReturn(DEFAULT_PASSWORD);
        when(guildProperties.getSyncCacheMinutes()).thenReturn(360);
    }

    @Test
    @DisplayName("Sync creates new players when guild is found")
    void syncGuild_createsPlayers() {
        AlbionGuildResponse albionGuild = AlbionGuildResponse.builder()
                .id("guild-1")
                .name(GUILD_NAME)
                .allianceName("Mi Alliance")
                .build();

        List<AlbionPlayerResponse> members = List.of(
                AlbionPlayerResponse.builder().id("p1").name("Player1").rank("Officer").build(),
                AlbionPlayerResponse.builder().id("p2").name("Player2").rank("Member").build()
        );

        when(albionApi.searchGuild(GUILD_NAME)).thenReturn(albionGuild);
        when(guildRepository.findByAlbionGuildId("guild-1")).thenReturn(Optional.empty());
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> {
            Guild g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
        when(albionApi.getGuildMembers("guild-1")).thenReturn(members);
        when(playerRepository.findByAlbionId(any())).thenReturn(Optional.empty());
        when(playerRepository.findByAlbionName(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("encoded");
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> {
            Player p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(playerRepository.findAllByGuildId(1L)).thenReturn(List.of());

        SyncGuildResponse response = guildSyncService.syncGuild(
                SyncGuildRequest.builder().guildName(GUILD_NAME).build());

        assertEquals(GUILD_NAME, response.getGuild());
        assertEquals(2, response.getPlayersImported());
        assertEquals(2, response.getCreated());
        assertEquals(0, response.getUpdated());
        verify(walletRepository, times(2)).save(any());
        verify(passwordEncoder, times(2)).encode(DEFAULT_PASSWORD);
    }

    @Test
    @DisplayName("Sync throws when guild does not exist")
    void syncGuild_guildNotFound() {
        when(albionApi.searchGuild(GUILD_NAME)).thenThrow(new GuildNotFoundException("Guild not found"));

        assertThrows(GuildNotFoundException.class, () ->
                guildSyncService.syncGuild(SyncGuildRequest.builder().guildName(GUILD_NAME).build()));
    }

    @Test
    @DisplayName("Sync updates existing player instead of duplicating")
    void syncGuild_updatesExistingPlayer() {
        AlbionGuildResponse albionGuild = AlbionGuildResponse.builder()
                .id("guild-1")
                .name(GUILD_NAME)
                .build();

        AlbionPlayerResponse member = AlbionPlayerResponse.builder()
                .id("p1")
                .name("Player1")
                .rank("Officer")
                .build();

        Player existing = Player.builder()
                .id(10L)
                .albionId("p1")
                .albionName("Player1")
                .discordName("Player1")
                .role(PlayerRole.PLAYER)
                .password("pwd")
                .active(true)
                .build();

        when(albionApi.searchGuild(GUILD_NAME)).thenReturn(albionGuild);
        when(guildRepository.findByAlbionGuildId("guild-1")).thenReturn(Optional.of(Guild.builder().id(1L).build()));
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));
        when(albionApi.getGuildMembers("guild-1")).thenReturn(List.of(member));
        when(playerRepository.findByAlbionId("p1")).thenReturn(Optional.of(existing));
        when(playerRepository.save(any(Player.class))).thenReturn(existing);
        when(playerRepository.findAllByGuildId(1L)).thenReturn(List.of(existing));

        SyncGuildResponse response = guildSyncService.syncGuild(
                SyncGuildRequest.builder().guildName(GUILD_NAME).build());

        assertEquals(1, response.getPlayersImported());
        assertEquals(0, response.getCreated());
        assertEquals(1, response.getUpdated());

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertEquals("Officer", captor.getValue().getRank());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sync matches player by albion name when albionId is missing locally")
    void syncGuild_updatesByAlbionName() {
        AlbionGuildResponse albionGuild = AlbionGuildResponse.builder()
                .id("guild-1")
                .name(GUILD_NAME)
                .build();

        Player existing = Player.builder()
                .id(5L)
                .albionName("OldName")
                .discordName("OldName")
                .role(PlayerRole.PLAYER)
                .password("pwd")
                .active(false)
                .build();

        when(albionApi.searchGuild(GUILD_NAME)).thenReturn(albionGuild);
        when(guildRepository.findByAlbionGuildId("guild-1")).thenReturn(Optional.empty());
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> {
            Guild g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
        when(albionApi.getGuildMembers("guild-1")).thenReturn(List.of(
                AlbionPlayerResponse.builder().id("new-id").name("OldName").rank("Member").build()));
        when(playerRepository.findByAlbionId("new-id")).thenReturn(Optional.empty());
        when(playerRepository.findByAlbionName("OldName")).thenReturn(Optional.of(existing));
        when(playerRepository.save(any(Player.class))).thenReturn(existing);
        when(playerRepository.findAllByGuildId(1L)).thenReturn(List.of(existing));

        SyncGuildResponse response = guildSyncService.syncGuild(
                SyncGuildRequest.builder().guildName(GUILD_NAME).build());

        assertEquals(1, response.getUpdated());
        assertTrue(existing.isActive());
        assertEquals("new-id", existing.getAlbionId());
    }

    @Test
    @DisplayName("Skips sync when cache is still fresh")
    void syncConfiguredGuildIfStale_skipsWhenFresh() {
        Guild guild = Guild.builder()
                .id(1L)
                .name(GUILD_NAME)
                .lastSync(LocalDateTime.now().minusMinutes(30))
                .build();

        when(guildRepository.findByName(GUILD_NAME)).thenReturn(Optional.of(guild));
        when(playerRepository.findAllByGuildId(1L)).thenReturn(List.of(
                Player.builder().id(1L).build(),
                Player.builder().id(2L).build()
        ));

        SyncGuildResponse response = guildSyncService.syncConfiguredGuildIfStale();

        assertTrue(response.isSkipped());
        assertEquals(2, response.getPlayersImported());
        assertEquals(0, response.getCreated());
        verifyNoInteractions(albionApi);
    }
}
