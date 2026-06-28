package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_build_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"template_id", "equipment_slot"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleBuildSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private RoleBuildTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_slot", nullable = false)
    private EquipmentSlot equipmentSlot;

    @Column(nullable = false)
    private String itemUniqueName;

    @Column(nullable = false)
    private String itemDisplayName;
}
