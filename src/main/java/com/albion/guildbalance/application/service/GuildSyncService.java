package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.albion.AlbionGuildResponse;
import com.albion.guildbalance.application.dto.albion.AlbionPlayerResponse;
import com.albion.guildbalance.application.dto.request.SyncGuildRequest;
import com.albion.guildbalance.application.dto.response.GuildInfoResponse;
import com.albion.guildbalance.application.dto.response.SyncGuildResponse;
import com.albion.guildbalance.application.exception.PlayerSyncException;
import com.albion.guildbalance.application.port.AlbionApiPort;
import com.albion.guildbalance.application.port.GuildRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Guild;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.domain.enums.PlayerRole;
import com.albion.guildbalance.infrastructure.config.GuildProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildSyncService {

    private static final String DEFAULT_RANK = "MEMBER";

    private final AlbionApiPort albionApi;
    private final GuildRepositoryPort guildRepository;
    private final PlayerRepositoryPort playerRepository;
    private final WalletRepositoryPort walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final GuildProperties guildProperties;

    @Transactional
    public SyncGuildResponse syncConfiguredGuild() {
        return syncGuild(SyncGuildRequest.builder()
                .guildName(guildProperties.getName())
                .build());
    }

    public SyncGuildResponse syncConfiguredGuildIfStale() {
        Optional<Guild> cached = guildRepository.findByName(guildProperties.getName());
        if (cached.isPresent() && isSyncFresh(cached.get())) {
            log.info("Guild sync skipped for {} — cache fresh (last sync: {})",
                    guildProperties.getName(), cached.get().getLastSync());
            return buildSkippedResponse(cached.get());
        }
        return syncConfiguredGuild();
    }

    public GuildInfoResponse getGuildInfo() {
        return GuildInfoResponse.builder()
                .name(guildProperties.getName())
                .defaultMemberPasswordHint(guildProperties.getDefaultMemberPassword())
                .build();
    }

    @Transactional
    public SyncGuildResponse syncGuild(SyncGuildRequest request) {
        String guildName = guildProperties.getName();
        if (request != null && request.getGuildName() != null && !request.getGuildName().isBlank()
                && !guildName.equalsIgnoreCase(request.getGuildName().trim())) {
            log.warn("Ignoring requested guild '{}'; only '{}' is allowed", request.getGuildName(), guildName);
        }

        log.info("Starting guild sync for: {}", guildName);

        AlbionGuildResponse albionGuild = albionApi.searchGuild(guildName);
        Guild guild = saveOrUpdateGuild(albionGuild);

        List<AlbionPlayerResponse> members;
        try {
            members = albionApi.getGuildMembers(albionGuild.getId());
        } catch (RuntimeException ex) {
            throw new PlayerSyncException("Failed to synchronize guild members", ex);
        }

        Set<String> syncedAlbionIds = new HashSet<>();
        int created = 0;
        int updated = 0;

        for (AlbionPlayerResponse member : members) {
            if (member.getId() == null || member.getName() == null || member.getName().isBlank()) {
                continue;
            }
            syncedAlbionIds.add(member.getId());
            if (syncMember(member, guild)) {
                created++;
            } else {
                updated++;
            }
        }

        int deactivated = deactivateMissingMembers(guild, syncedAlbionIds);

        log.info("Guild sync completed. Created: {}, Updated: {}, Deactivated: {}", created, updated, deactivated);

        return SyncGuildResponse.builder()
                .guild(guild.getName())
                .playersImported(created + updated)
                .created(created)
                .updated(updated)
                .skipped(false)
                .lastSyncAt(guild.getLastSync())
                .build();
    }

    private SyncGuildResponse buildSkippedResponse(Guild guild) {
        int memberCount = playerRepository.findAllByGuildId(guild.getId()).size();
        return SyncGuildResponse.builder()
                .guild(guild.getName())
                .playersImported(memberCount)
                .created(0)
                .updated(0)
                .skipped(true)
                .lastSyncAt(guild.getLastSync())
                .build();
    }

    private boolean isSyncFresh(Guild guild) {
        if (guild.getLastSync() == null) {
            return false;
        }
        return guild.getLastSync()
                .plusMinutes(guildProperties.getSyncCacheMinutes())
                .isAfter(LocalDateTime.now());
    }

    private int deactivateMissingMembers(Guild guild, Set<String> syncedAlbionIds) {
        int count = 0;
        for (Player player : playerRepository.findAllByGuildId(guild.getId())) {
            if (player.getAlbionId() != null && !syncedAlbionIds.contains(player.getAlbionId()) && player.isActive()) {
                player.setActive(false);
                playerRepository.save(player);
                count++;
            }
        }
        return count;
    }

    private Guild saveOrUpdateGuild(AlbionGuildResponse albionGuild) {
        Guild guild = guildRepository.findByAlbionGuildId(albionGuild.getId())
                .orElseGet(() -> Guild.builder()
                        .albionGuildId(albionGuild.getId())
                        .build());

        guild.setName(albionGuild.getName());
        guild.setAlliance(emptyToNull(albionGuild.getAllianceName()));
        guild.setLastSync(LocalDateTime.now());

        return guildRepository.save(guild);
    }

    private boolean syncMember(AlbionPlayerResponse member, Guild guild) {
        Optional<Player> existing = playerRepository.findByAlbionId(member.getId())
                .or(() -> playerRepository.findByAlbionName(member.getName()));

        if (existing.isPresent()) {
            Player player = existing.get();
            player.setAlbionId(member.getId());
            player.setAlbionName(member.getName());
            player.setRank(resolveRank(member));
            player.setGuild(guild);
            player.setActive(true);
            playerRepository.save(player);
            return false;
        }

        Player player = Player.builder()
                .albionId(member.getId())
                .albionName(member.getName())
                .discordName(member.getName())
                .rank(resolveRank(member))
                .role(PlayerRole.PLAYER)
                .password(passwordEncoder.encode(guildProperties.getDefaultMemberPassword()))
                .guild(guild)
                .active(true)
                .build();

        Player saved = playerRepository.save(player);
        walletRepository.save(Wallet.builder()
                .player(saved)
                .balance(BigDecimal.ZERO)
                .build());
        return true;
    }

    private String resolveRank(AlbionPlayerResponse member) {
        if (member.getRank() != null && !member.getRank().isBlank()) {
            return member.getRank();
        }
        return DEFAULT_RANK;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
