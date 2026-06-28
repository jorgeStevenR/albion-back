package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepositoryPort walletRepository;
    private final PlayerRepositoryPort playerRepository;

    @Transactional
    public void credit(Player player, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Credit amount must be positive");
        }
        Wallet wallet = getOrCreateWallet(player);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void debit(Player player, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Debit amount must be positive");
        }
        Wallet wallet = getOrCreateWallet(player);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    private Wallet getOrCreateWallet(Player player) {
        return walletRepository.findByPlayerId(player.getId())
                .orElseGet(() -> {
                    Player managed = playerRepository.findById(player.getId())
                            .orElseThrow(() -> new BusinessException("Player not found"));
                    return Wallet.builder().player(managed).balance(BigDecimal.ZERO).build();
                });
    }
}
