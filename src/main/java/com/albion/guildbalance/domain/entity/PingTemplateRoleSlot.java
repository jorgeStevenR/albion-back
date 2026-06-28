package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ping_template_role_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PingTemplateRoleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AvalonPingTemplate template;

    @Column(name = "slot_key", nullable = false)
    private String slotKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    @OneToMany(mappedBy = "partySlot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PingTemplateBuildSlot> buildSlots = new ArrayList<>();

    @OneToMany(mappedBy = "partySlot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PingTemplateSwapItem> swapItems = new ArrayList<>();
}
