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
public class AlbionItemResponse {

    private String uniqueName;
    private String displayName;
    private String displayNameEs;
    /** Nombre en español listo para mostrar, con tier y encantamiento. */
    private String label;
    private EquipmentSlot equipmentSlot;
    private int tier;
    private int enchantment;
    private int quality;
    private String iconUrl;

}
