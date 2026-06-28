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

    private static final BigDecimal LOOT_NET_MULTIPLIER = new BigDecimal("0.80");
    private static final int SCALE = 2;

    public BigDecimal calculateGrossValue(LootItem loot) {
        return loot.getMarketValue()
                .multiply(BigDecimal.valueOf(loot.getQuantity()))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Effective value shown per loot line (bags = gross, chest items = after 20% guild cut). */
    public BigDecimal calculateLootLineValue(LootItem loot) {
        BigDecimal gross = calculateGrossValue(loot);
        if (loot.getType() == LootType.ITEM) {
            return gross.multiply(LOOT_NET_MULTIPLIER).setScale(SCALE, RoundingMode.HALF_UP);
        }
        return gross;
    }

    public BigDecimal calculateBagGross(List<LootItem> lootItems) {
        return lootItems.stream()
                .filter(loot -> loot.getType() == LootType.BAG)
                .map(BalanceCalculator::calculateGrossValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateChestNet(List<LootItem> lootItems) {
        return lootItems.stream()
                .filter(loot -> loot.getType() == LootType.ITEM)
                .map(BalanceCalculator::calculateLootLineValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBagNet(List<LootItem> lootItems, BigDecimal mapsCost) {
        BigDecimal cost = mapsCost != null ? mapsCost : BigDecimal.ZERO;
        BigDecimal net = calculateBagGross(lootItems).subtract(cost);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return net.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalBalance(List<LootItem> lootItems, BigDecimal mapsCost) {
        return calculateBagNet(lootItems, mapsCost)
                .add(calculateChestNet(lootItems))
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
