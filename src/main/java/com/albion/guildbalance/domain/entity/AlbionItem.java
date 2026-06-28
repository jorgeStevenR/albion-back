package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "albion_items", indexes = {
        @Index(name = "idx_albion_item_name", columnList = "displayName"),
        @Index(name = "idx_albion_item_name_es", columnList = "displayNameEs"),
        @Index(name = "idx_albion_item_slot", columnList = "equipmentSlot"),
        @Index(name = "idx_albion_item_tier", columnList = "tier"),
        @Index(name = "idx_albion_item_enchant", columnList = "enchantment")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbionItem {

    @Id
    @Column(length = 120)
    private String uniqueName;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private String displayNameEs = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot equipmentSlot;

    private int tier;

    @Column(nullable = false)
    @Builder.Default
    private int enchantment = 0;

    /** Calidad normal (1) — las variantes Buena/Obra maestra son en el juego, no en plantillas */
    @Column(nullable = false)
    @Builder.Default
    private int quality = 1;

    @Column(nullable = false, length = 512)
    @Builder.Default
    private String searchText = "";
}
