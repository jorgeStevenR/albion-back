package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.response.*;
import com.albion.guildbalance.domain.entity.AvalonRun;
import com.albion.guildbalance.domain.entity.Distribution;
import com.albion.guildbalance.domain.entity.Sale;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuildStatsService {

    private final WalletJpaRepository walletRepository;
    private final SaleJpaRepository saleRepository;
    private final DistributionJpaRepository distributionRepository;
    private final AvalonRunJpaRepository avalonRunRepository;
    private final LootItemJpaRepository lootItemRepository;
    private final AvalonRoleRegistrationJpaRepository registrationRepository;
    private final PlayerJpaRepository playerRepository;

    @Transactional(readOnly = true)
    public GuildStatsResponse getStats() {
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        BigDecimal treasury = saleRepository.sumAllFinalPrices();
        BigDecimal unsoldItems = lootItemRepository.sumMarketValueBySaleStatus(LootSaleStatus.UNSOLD);
        BigDecimal unsoldBags = lootItemRepository.sumMarketValueBySaleStatus(LootSaleStatus.NOT_APPLICABLE);
        BigDecimal unsoldEffective = unsoldItems.add(unsoldBags).multiply(BigDecimal.valueOf(0.8));

        List<Object[]> callerRows = registrationRepository.countAvalonsByPlayerForRole(
                RoleType.CALLER, RegistrationStatus.ACTIVE);

        List<CallerStatResponse> topCallers = callerRows.stream()
                .limit(5)
                .map(row -> CallerStatResponse.builder()
                        .playerId((Long) row[0])
                        .playerName((String) row[1])
                        .avalonCount((Long) row[2])
                        .build())
                .toList();

        List<PlayerPaymentStatResponse> weeklyRanking = distributionRepository.sumAmountByPlayerSince(weekStart)
                .stream()
                .map(row -> PlayerPaymentStatResponse.builder()
                        .playerId((Long) row[0])
                        .playerName((String) row[1])
                        .totalPaid((BigDecimal) row[2])
                        .build())
                .toList();

        return GuildStatsResponse.builder()
                .treasuryBalance(treasury)
                .unsoldLootValue(unsoldEffective)
                .totalMemberWallets(walletRepository.sumAllBalances())
                .weeklySalesIncome(saleRepository.sumFinalPricesSince(weekStart))
                .weeklyMemberEarnings(distributionRepository.sumAmountSince(weekStart))
                .totalAvalons(avalonRunRepository.count())
                .openAvalons(avalonRunRepository.countByStatus(AvalonStatus.OPEN))
                .finishedAvalons(avalonRunRepository.countByStatus(AvalonStatus.FINISHED))
                .closedAvalons(avalonRunRepository.countByStatus(AvalonStatus.CLOSED))
                .weeklyAvalons(avalonRunRepository.countByDateGreaterThanEqual(weekStart.toLocalDate()))
                .activeMembers(playerRepository.countByActiveTrueAndGuildIsNotNull())
                .topCallers(topCallers)
                .weeklyStats(buildWeeklyStats())
                .weeklyPaymentRanking(weeklyRanking)
                .build();
    }

    @Transactional(readOnly = true)
    public List<GuildTransactionResponse> getTransactions() {
        List<GuildTransactionResponse> transactions = new ArrayList<>();

        for (Sale sale : saleRepository.findAllByOrderByCreatedAtDesc()) {
            transactions.add(GuildTransactionResponse.builder()
                    .id(sale.getId())
                    .type("SALE")
                    .amount(sale.getFinalPrice())
                    .description("Venta: " + sale.getLootItem().getName() + " → " + sale.getBuyer().getAlbionName())
                    .avalonId(sale.getLootItem().getAvalonRun().getId())
                    .avalonZone(sale.getLootItem().getAvalonRun().getZone())
                    .playerName(sale.getBuyer().getAlbionName())
                    .createdAt(sale.getCreatedAt())
                    .build());
        }

        for (Distribution dist : distributionRepository.findAllByOrderByCreatedAtDesc()) {
            transactions.add(GuildTransactionResponse.builder()
                    .id(dist.getId())
                    .type("DISTRIBUTION")
                    .amount(dist.getAmount().negate())
                    .description("Reparto avalon → " + dist.getPlayer().getAlbionName())
                    .avalonId(dist.getAvalonRun().getId())
                    .avalonZone(dist.getAvalonRun().getZone())
                    .playerName(dist.getPlayer().getAlbionName())
                    .createdAt(dist.getCreatedAt())
                    .build());
        }

        transactions.sort(Comparator.comparing(GuildTransactionResponse::getCreatedAt).reversed());
        return transactions.stream().limit(100).toList();
    }

    private List<WeeklyGuildStatResponse> buildWeeklyStats() {
        List<WeeklyGuildStatResponse> stats = new ArrayList<>();
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 3; i >= 0; i--) {
            LocalDate weekStart = monday.minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            LocalDateTime startDt = weekStart.atStartOfDay();
            LocalDateTime endDt = weekEnd.plusDays(1).atStartOfDay();

            long avalonCount = avalonRunRepository.findAll().stream()
                    .filter(a -> !a.getDate().isBefore(weekStart) && !a.getDate().isAfter(weekEnd))
                    .count();

            BigDecimal sales = saleRepository.findAll().stream()
                    .filter(s -> !s.getCreatedAt().isBefore(startDt) && s.getCreatedAt().isBefore(endDt))
                    .map(Sale::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal earnings = distributionRepository.findAll().stream()
                    .filter(d -> !d.getCreatedAt().isBefore(startDt) && d.getCreatedAt().isBefore(endDt))
                    .map(Distribution::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.add(WeeklyGuildStatResponse.builder()
                    .weekLabel(weekStart.getDayOfMonth() + "/" + weekStart.getMonthValue())
                    .avalonCount(avalonCount)
                    .salesIncome(sales)
                    .memberEarnings(earnings)
                    .build());
        }
        return stats;
    }
}
