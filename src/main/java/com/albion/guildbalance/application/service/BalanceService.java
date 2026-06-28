package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.response.BalanceResponse;
import com.albion.guildbalance.application.dto.response.DistributionResponse;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.DistributionRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final PlayerRepositoryPort playerRepository;
    private final WalletRepositoryPort walletRepository;
    private final DistributionRepositoryPort distributionRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public BalanceResponse getPlayerBalance(Long playerId) {
        log.debug("Fetching balance for player {}", playerId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        BigDecimal walletBalance = walletRepository.findByPlayerId(playerId)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        List<DistributionResponse> distributions = mapper.toDistributionResponseList(
                distributionRepository.findByPlayerId(playerId));

        return BalanceResponse.builder()
                .playerId(playerId)
                .albionName(player.getAlbionName())
                .walletBalance(walletBalance)
                .distributions(distributions)
                .build();
    }
}
