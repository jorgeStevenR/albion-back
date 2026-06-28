package com.albion.guildbalance.application.mapper;

import com.albion.guildbalance.application.dto.response.*;
import com.albion.guildbalance.domain.entity.*;
import com.albion.guildbalance.domain.service.BalanceCalculator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    PlayerResponse toPlayerResponse(Player player);

    List<PlayerResponse> toPlayerResponseList(List<Player> players);

    @Mapping(target = "participants", source = "participants")
    @Mapping(target = "lootItems", source = "lootItems")
    @Mapping(target = "createdByPlayerId", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.albionName")
    AvalonRunResponse toAvalonRunResponse(AvalonRun avalonRun);

    List<AvalonRunResponse> toAvalonRunResponseList(List<AvalonRun> avalonRuns);

    @Mapping(target = "playerId", source = "player.id")
    @Mapping(target = "albionName", source = "player.albionName")
    ParticipantResponse toParticipantResponse(AvalonParticipant participant);

    @Mapping(target = "effectiveValue", source = ".", qualifiedByName = "calculateEffectiveValue")
    LootItemResponse toLootItemResponse(LootItem lootItem);

    @Mapping(target = "avalonId", source = "avalonRun.id")
    @Mapping(target = "avalonZone", source = "avalonRun.zone")
    DistributionResponse toDistributionResponse(Distribution distribution);

    List<DistributionResponse> toDistributionResponseList(List<Distribution> distributions);

    @Mapping(target = "lootItemId", source = "lootItem.id")
    @Mapping(target = "lootItemName", source = "lootItem.name")
    @Mapping(target = "buyerId", source = "buyer.id")
    @Mapping(target = "buyerName", source = "buyer.albionName")
    SaleResponse toSaleResponse(Sale sale);

    @Named("calculateEffectiveValue")
    default java.math.BigDecimal calculateEffectiveValue(LootItem loot) {
        return BalanceCalculator.calculateLootLineValue(loot);
    }
}
