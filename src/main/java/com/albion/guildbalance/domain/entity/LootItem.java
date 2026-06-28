package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.LootType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loot_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LootItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ava_id", nullable = false)
    private AvalonRun avalonRun;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LootType type;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal marketValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LootSaleStatus saleStatus = LootSaleStatus.UNSOLD;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (quantity == null) {
            quantity = 1;
        }
        if (saleStatus == null) {
            saleStatus = type == LootType.BAG ? LootSaleStatus.NOT_APPLICABLE : LootSaleStatus.UNSOLD;
        }
    }
}
