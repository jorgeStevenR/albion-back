package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.EquipmentSlot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ping_template_build_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"party_slot_id", "equipment_slot"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PingTemplateBuildSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_slot_id", nullable = false)
    private PingTemplateRoleSlot partySlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_slot", nullable = false)
    private EquipmentSlot equipmentSlot;

    @Column(nullable = false)
    private String itemUniqueName;

    @Column(nullable = false)
    private String itemDisplayName;
}
