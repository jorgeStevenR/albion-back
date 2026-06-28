package com.albion.guildbalance.domain.entity;

import com.albion.guildbalance.domain.enums.ParticipantType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avalon_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ava_id", "player_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvalonParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ava_id", nullable = false)
    private AvalonRun avalonRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantType participantType;
}
