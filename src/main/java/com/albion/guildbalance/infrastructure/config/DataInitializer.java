package com.albion.guildbalance.infrastructure.config;

import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.domain.enums.PlayerRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final String DEFAULT_ADMIN = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final PlayerRepositoryPort playerRepository;
    private final WalletRepositoryPort walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDefaultAdmin() {
        return args -> playerRepository.findByAlbionName(DEFAULT_ADMIN).ifPresentOrElse(
                this::syncAdmin,
                this::createAdmin
        );
    }

    private void createAdmin() {
        log.info("Creating default admin user");
        Player admin = Player.builder()
                .albionName(DEFAULT_ADMIN)
                .discordName(DEFAULT_ADMIN)
                .role(PlayerRole.ADMIN)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .active(true)
                .build();
        Player saved = playerRepository.save(admin);
        walletRepository.save(Wallet.builder()
                .player(saved)
                .balance(BigDecimal.ZERO)
                .build());
    }

    private void syncAdmin(Player admin) {
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        admin.setActive(true);
        admin.setRole(PlayerRole.ADMIN);
        playerRepository.save(admin);
        walletRepository.findByPlayerId(admin.getId()).orElseGet(() ->
                walletRepository.save(Wallet.builder()
                        .player(admin)
                        .balance(BigDecimal.ZERO)
                        .build()));
        log.info("Default admin user synchronized");
    }
}
