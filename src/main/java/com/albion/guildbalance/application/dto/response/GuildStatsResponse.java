package com.albion.guildbalance.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class GuildStatsResponse {

    private BigDecimal treasuryBalance;
    private BigDecimal unsoldLootValue;
    private BigDecimal totalMemberWallets;
    private BigDecimal weeklySalesIncome;
    private BigDecimal weeklyMemberEarnings;
    private long totalAvalons;
    private long openAvalons;
    private long finishedAvalons;
    private long closedAvalons;
    private long weeklyAvalons;
    private long activeMembers;
    private List<CallerStatResponse> topCallers;
    private List<WeeklyGuildStatResponse> weeklyStats;
    private List<PlayerPaymentStatResponse> weeklyPaymentRanking;
}
