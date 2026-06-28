package com.albion.guildbalance.domain.service;

import com.albion.guildbalance.domain.entity.AvalonParticipant;
import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.domain.enums.LootType;
import com.albion.guildbalance.domain.enums.ParticipantType;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public class BalanceCalculator {

    private static final BigDecimal ITEM_DISCOUNT = new BigDecimal("0.80");
    private static final int SCALE = 2;

    public BigDecimal calculateLootValue(LootItem loot) {
        BigDecimal total = loot.getMarketValue().multiply(BigDecimal.valueOf(loot.getQuantity()));
        if (loot.getType() == LootType.ITEM) {
            return total.multiply(ITEM_DISCOUNT).setScale(SCALE, RoundingMode.HALF_UP);
        }
        return total.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalBalance(List<LootItem> lootItems) {
        return lootItems.stream()
                .map(BalanceCalculator::calculateLootValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public double calculateTotalWeight(List<AvalonParticipant> participants) {
        return participants.stream()
                .mapToDouble(p -> p.getParticipantType().getWeight())
                .sum();
    }

    public BigDecimal calculateParticipantShare(BigDecimal totalBalance, double totalWeight, ParticipantType type) {
        if (totalWeight == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal sharePerWeight = totalBalance.divide(
                BigDecimal.valueOf(totalWeight), SCALE, RoundingMode.HALF_UP);
        return sharePerWeight.multiply(BigDecimal.valueOf(type.getWeight()))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSalePrice(BigDecimal originalValue, BigDecimal discountPercent) {
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                discountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return originalValue.multiply(discountMultiplier).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
