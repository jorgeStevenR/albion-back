package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull(message = "Loot item ID is required")
    private Long lootItemId;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.0", message = "Discount must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100")
    @Builder.Default
    private BigDecimal discount = new BigDecimal("20.00");
}
