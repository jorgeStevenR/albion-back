package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonRoleRegistration;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AvalonRoleRegistrationJpaRepository extends JpaRepository<AvalonRoleRegistration, Long> {

    @EntityGraph(attributePaths = "player")
    List<AvalonRoleRegistration> findByAvalonRun_IdAndStatus(Long avalonId, RegistrationStatus status);

    @EntityGraph(attributePaths = "player")
    List<AvalonRoleRegistration> findByAvalonRun_IdAndRoleTypeAndStatus(
            Long avalonId, RoleType roleType, RegistrationStatus status);

    Optional<AvalonRoleRegistration> findByAvalonRun_IdAndPlayer_IdAndStatus(
            Long avalonId, Long playerId, RegistrationStatus status);

    Optional<AvalonRoleRegistration> findByAvalonRun_IdAndPlayer_IdAndRoleTypeAndStatus(
            Long avalonId, Long playerId, RoleType roleType, RegistrationStatus status);

    Optional<AvalonRoleRegistration> findByAvalonRun_IdAndPlayer_IdAndSlotKeyAndStatus(
            Long avalonId, Long playerId, String slotKey, RegistrationStatus status);

    @Query("""
            SELECT r.player.id, r.player.albionName, COUNT(DISTINCT r.avalonRun.id)
            FROM AvalonRoleRegistration r
            WHERE r.roleType = :roleType AND r.status = :status
            GROUP BY r.player.id, r.player.albionName
            ORDER BY COUNT(DISTINCT r.avalonRun.id) DESC
            """)
    List<Object[]> countAvalonsByPlayerForRole(RoleType roleType, RegistrationStatus status);
}
