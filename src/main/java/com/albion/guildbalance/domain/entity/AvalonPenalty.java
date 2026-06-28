package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.PenaltyDirection;
import com.albion.guildbalance.domain.enums.PenaltyStatus;
import com.albion.guildbalance.domain.enums.PenaltyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "avalon_penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avalon_id", nullable = false)
    private AvalonRun avalonRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyType type;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.APPLIED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Player createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_player_id")
    private Player relatedPlayer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
