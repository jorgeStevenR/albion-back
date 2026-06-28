package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.AvalonRoleRegistration;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;

import java.util.List;
import java.util.Optional;

public interface AvalonRoleRegistrationRepositoryPort {

    AvalonRoleRegistration save(AvalonRoleRegistration registration);

    List<AvalonRoleRegistration> findActiveByAvalonId(Long avalonId);

    List<AvalonRoleRegistration> findActiveByAvalonIdAndRoleType(Long avalonId, RoleType roleType);

    Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerId(Long avalonId, Long playerId);

    Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerIdAndRoleType(
            Long avalonId, Long playerId, RoleType roleType);

    Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerIdAndSlotKey(
            Long avalonId, Long playerId, String slotKey);

    long countActiveByAvalonId(Long avalonId);
}
