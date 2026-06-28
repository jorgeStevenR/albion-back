package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private Long id;
    private Long lootItemId;
    private String lootItemName;
    private Long buyerId;
    private String buyerName;
    private BigDecimal originalValue;
    private BigDecimal discount;
    private BigDecimal finalPrice;
}
