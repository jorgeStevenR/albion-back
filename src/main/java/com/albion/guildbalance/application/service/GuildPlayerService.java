package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.response.AvalonParticipationResponse;
import com.albion.guildbalance.application.dto.response.GuildPlayerDetailResponse;
import com.albion.guildbalance.application.dto.response.GuildPlayerResponse;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.AvalonParticipantRepositoryPort;
import com.albion.guildbalance.application.port.DistributionRepositoryPort;
import com.albion.guildbalance.application.port.GuildRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonParticipant;
import com.albion.guildbalance.domain.entity.Guild;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.infrastructure.config.GuildProperties;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonParticipantJpaRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.DistributionJpaRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildPlayerService {

    private final PlayerRepositoryPort playerRepository;
    private final GuildRepositoryPort guildRepository;
    private final GuildProperties guildProperties;
    private final WalletRepositoryPort walletRepository;
    private final DistributionRepositoryPort distributionRepository;
    private final AvalonParticipantRepositoryPort avalonParticipantRepository;
    private final DistributionJpaRepository distributionJpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final AvalonParticipantJpaRepository avalonParticipantJpaRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<GuildPlayerResponse> findAllSynced() {
        Guild guild = guildRepository.findByName(guildProperties.getName()).orElse(null);
        if (guild == null) {
            return List.of();
        }

        List<Player> players = playerRepository.findAllByGuildId(guild.getId());
        if (players.isEmpty()) {
            return List.of();
        }

        List<Long> playerIds = players.stream().map(Player::getId).toList();
        Map<Long, BigDecimal> balances = loadBalances(playerIds);
        Map<Long, BigDecimal> earned = loadTotalEarned(playerIds);
        Map<Long, Long> avalonCounts = loadAvalonCounts(playerIds);

        return players.stream()
                .map(player -> GuildPlayerResponse.builder()
                        .id(player.getId())
                        .albionName(player.getAlbionName())
                        .rank(player.getRank())
                        .active(player.isActive())
                        .balance(balances.getOrDefault(player.getId(), BigDecimal.ZERO))
                        .totalEarned(earned.getOrDefault(player.getId(), BigDecimal.ZERO))
                        .avalonCount(avalonCounts.getOrDefault(player.getId(), 0L))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public GuildPlayerDetailResponse findById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        BigDecimal balance = walletRepository.findByPlayerId(id)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        List<AvalonParticipant> participations = avalonParticipantRepository.findByPlayerId(id);

        return GuildPlayerDetailResponse.builder()
                .id(player.getId())
                .albionName(player.getAlbionName())
                .rank(player.getRank())
                .active(player.isActive())
                .guildName(player.getGuild() != null ? player.getGuild().getName() : null)
                .balance(balance)
                .distributions(mapper.toDistributionResponseList(
                        distributionRepository.findByPlayerId(id)))
                .avalonParticipations(participations.stream()
                        .map(this::toParticipationResponse)
                        .toList())
                .build();
    }

    private Map<Long, BigDecimal> loadBalances(List<Long> playerIds) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Object[] row : walletJpaRepository.findBalancesByPlayerIds(playerIds)) {
            map.put((Long) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    private Map<Long, BigDecimal> loadTotalEarned(List<Long> playerIds) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Object[] row : distributionJpaRepository.sumAmountByPlayerIds(playerIds)) {
            map.put((Long) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    private Map<Long, Long> loadAvalonCounts(List<Long> playerIds) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : avalonParticipantJpaRepository.countByPlayerIds(playerIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private AvalonParticipationResponse toParticipationResponse(AvalonParticipant participant) {
        return AvalonParticipationResponse.builder()
                .avalonId(participant.getAvalonRun().getId())
                .date(participant.getAvalonRun().getDate())
                .zone(participant.getAvalonRun().getZone())
                .participantType(participant.getParticipantType())
                .build();
    }
}
