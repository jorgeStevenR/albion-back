package com.albion.guildbalance.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avalon_slot_swap_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonSlotSwapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_slot_id", nullable = false)
    private AvalonRoleSlot roleSlot;

    @Column(nullable = false)
    private String itemUniqueName;

    @Column(nullable = false)
    private String itemDisplayName;

    @Column(length = 200)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
