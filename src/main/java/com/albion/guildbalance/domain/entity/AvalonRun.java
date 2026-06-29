package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.AvalonStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "avalon_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private String zone;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AvalonStatus status = AvalonStatus.OPEN;

    @Column(nullable = false)
    @Builder.Default
    private boolean registrationsOpen = true;

    @Column(nullable = false)
    @Builder.Default
    private int mapsThrown = 0;

    /** Silver cost per map; total deduction = mapsThrown × mapsCost. */
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal mapsCost = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Player createdBy;

    @OneToMany(mappedBy = "avalonRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<AvalonParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "avalonRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<LootItem> lootItems = new ArrayList<>();
}
