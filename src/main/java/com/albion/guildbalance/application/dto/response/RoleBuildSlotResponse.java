package com.albion.guildbalance.application.dto.response;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleBuildSlotResponse {

    private EquipmentSlot equipmentSlot;
    private String itemUniqueName;
    private String itemDisplayName;
    private String iconUrl;
}
