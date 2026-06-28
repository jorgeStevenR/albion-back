package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.LootType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LootItemResponse {

    private Long id;
    private String name;
    private LootType type;
    private Integer quantity;
    private BigDecimal marketValue;
    private BigDecimal effectiveValue;
    private LootSaleStatus saleStatus;
    private LocalDateTime createdAt;
}
