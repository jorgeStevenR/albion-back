package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "avalon_role_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"avalon_id", "slot_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonRoleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avalon_id", nullable = false)
    private AvalonRun avalonRun;

    @Column(name = "slot_key", nullable = false)
    private String slotKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    @Column(nullable = false)
    private int maxPlayers;

    @Column(nullable = false)
    @Builder.Default
    private int currentPlayers = 0;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @OneToMany(mappedBy = "roleSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvalonSlotBuildItem> buildItems = new ArrayList<>();

    @OneToMany(mappedBy = "roleSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvalonSlotSwapItem> swapItems = new ArrayList<>();
}
