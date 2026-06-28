package com.albion.guildbalance.domain.service;

import com.albion.guildbalance.domain.entity.AvalonParticipant;
import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.LootType;
import com.albion.guildbalance.domain.enums.ParticipantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceCalculatorTest {

    @Test
    @DisplayName("BAG loot keeps gross value on line (maps deducted at total level)")
    void calculateLootLineValue_bag_keepsGross() {
        LootItem bag = LootItem.builder()
                .type(LootType.BAG)
                .quantity(2)
                .marketValue(new BigDecimal("1000000"))
                .build();

        assertEquals(new BigDecimal("2000000.00"), BalanceCalculator.calculateLootLineValue(bag));
    }

    @Test
    @DisplayName("ITEM loot applies 20% guild discount")
    void calculateLootLineValue_item_appliesDiscount() {
        LootItem item = LootItem.builder()
                .type(LootType.ITEM)
                .quantity(1)
                .marketValue(new BigDecimal("10000000"))
                .build();

        assertEquals(new BigDecimal("8000000.00"), BalanceCalculator.calculateLootLineValue(item));
    }

    @Test
    @DisplayName("Total balance: bags minus maps + chest after 20%")
    void calculateTotalBalance_example() {
        List<LootItem> loot = List.of(
                LootItem.builder().type(LootType.BAG).quantity(1).marketValue(new BigDecimal("20000000")).build(),
                LootItem.builder().type(LootType.ITEM).quantity(1).marketValue(new BigDecimal("100000000")).build()
        );

        BigDecimal total = BalanceCalculator.calculateTotalBalance(loot, new BigDecimal("4000000"));

        assertEquals(new BigDecimal("96000000.00"), total);
    }

    @Test
    @DisplayName("Map cost cannot reduce bags below zero")
    void calculateBagNet_floorAtZero() {
        List<LootItem> loot = List.of(
                LootItem.builder().type(LootType.BAG).quantity(1).marketValue(new BigDecimal("3000000")).build()
        );

        assertEquals(BigDecimal.ZERO.setScale(2), BalanceCalculator.calculateBagNet(loot, new BigDecimal("4000000")));
    }

    @Test
    @DisplayName("Total weight: 10 players + 1 scout + guild = 12.2")
    void calculateTotalWeight_example() {
        List<AvalonParticipant> participants = List.of(
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.PLAYER),
                createParticipant(ParticipantType.SCOUT),
                createParticipant(ParticipantType.GUILD)
        );

        assertEquals(12.2, BalanceCalculator.calculateTotalWeight(participants));
    }

    @Test
    @DisplayName("Participant share calculation")
    void calculateParticipantShare() {
        BigDecimal total = new BigDecimal("12200000.00");
        BigDecimal scoutShare = BalanceCalculator.calculateParticipantShare(total, 12.2, ParticipantType.SCOUT);
        BigDecimal playerShare = BalanceCalculator.calculateParticipantShare(total, 12.2, ParticipantType.PLAYER);

        assertEquals(new BigDecimal("1200000.00"), scoutShare);
        assertEquals(new BigDecimal("1000000.00"), playerShare);
    }

    @Test
    @DisplayName("Sale price with 20% discount")
    void calculateSalePrice() {
        BigDecimal price = BalanceCalculator.calculateSalePrice(
                new BigDecimal("10000000"), new BigDecimal("20"));
        assertEquals(new BigDecimal("8000000.00"), price);
    }

    private AvalonParticipant createParticipant(ParticipantType type) {
        return AvalonParticipant.builder()
                .player(Player.builder().id(1L).build())
                .participantType(type)
                .build();
    }
}
