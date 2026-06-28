package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.LootType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class LootItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private LootType type;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;

    @NotNull(message = "Market value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Market value must be greater than 0")
    private BigDecimal marketValue;
}
