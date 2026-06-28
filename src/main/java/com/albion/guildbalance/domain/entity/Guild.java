package com.albion.guildbalance.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guilds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String albionGuildId;

    @Column(nullable = false)
    private String name;

    private String alliance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastSync;

    @OneToMany(mappedBy = "guild", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
