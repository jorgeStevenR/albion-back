package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleBuildSlotRequest {

    @NotNull
    private EquipmentSlot equipmentSlot;

    @NotBlank
    private String itemUniqueName;

    @NotBlank
    private String itemDisplayName;
}
