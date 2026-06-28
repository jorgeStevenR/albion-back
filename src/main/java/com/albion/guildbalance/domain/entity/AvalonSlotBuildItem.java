package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avalon_slot_build_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_slot_id", "equipment_slot"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonSlotBuildItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_slot_id", nullable = false)
    private AvalonRoleSlot roleSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_slot", nullable = false)
    private EquipmentSlot equipmentSlot;

    @Column(nullable = false)
    private String itemUniqueName;

    @Column(nullable = false)
    private String itemDisplayName;
}
