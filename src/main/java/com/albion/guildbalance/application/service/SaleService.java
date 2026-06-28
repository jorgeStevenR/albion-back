package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.SaleRequest;
import com.albion.guildbalance.application.dto.response.SaleResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.LootItemRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.SaleRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.LootItem;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Sale;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.LootType;
import com.albion.guildbalance.domain.service.BalanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepositoryPort saleRepository;
    private final LootItemRepositoryPort lootItemRepository;
    private final PlayerRepositoryPort playerRepository;
    private final WalletRepositoryPort walletRepository;
    private final EntityMapper mapper;

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        log.info("Creating sale for loot item {} to buyer {}", request.getLootItemId(), request.getBuyerId());

        LootItem lootItem = lootItemRepository.findById(request.getLootItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Loot item not found with id: " + request.getLootItemId()));

        if (lootItem.getType() != LootType.ITEM) {
            throw new BusinessException("Only ITEM type loot can be sold");
        }
        if (lootItem.getSaleStatus() == LootSaleStatus.SOLD) {
            throw new BusinessException("This loot item has already been sold");
        }
        AvalonStatus status = lootItem.getAvalonRun().getStatus();
        if (status != AvalonStatus.FINISHED && status != AvalonStatus.CLOSED) {
            throw new BusinessException("El loot solo se puede vender después de terminar la avaloniana");
        }

        Player buyer = playerRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + request.getBuyerId()));

        BigDecimal originalValue = lootItem.getMarketValue().multiply(BigDecimal.valueOf(lootItem.getQuantity()));
        BigDecimal finalPrice = BalanceCalculator.calculateSalePrice(originalValue, request.getDiscount());

        Wallet wallet = walletRepository.findByPlayerId(buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for buyer"));

        if (wallet.getBalance().compareTo(finalPrice) < 0) {
            throw new BusinessException("Insufficient wallet balance for purchase");
        }

        wallet.setBalance(wallet.getBalance().subtract(finalPrice));
        walletRepository.save(wallet);

        lootItem.setSaleStatus(LootSaleStatus.SOLD);
        lootItemRepository.save(lootItem);

        Sale sale = Sale.builder()
                .lootItem(lootItem)
                .buyer(buyer)
                .originalValue(originalValue)
                .discount(request.getDiscount())
                .finalPrice(finalPrice)
                .build();

        return mapper.toSaleResponse(saleRepository.save(sale));
    }
}
