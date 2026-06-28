package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.AvalonRoleSlot;
import com.albion.guildbalance.domain.enums.RoleType;

import java.util.List;
import java.util.Optional;

public interface AvalonRoleSlotRepositoryPort {

    AvalonRoleSlot save(AvalonRoleSlot slot);

    List<AvalonRoleSlot> findByAvalonId(Long avalonId);

    Optional<AvalonRoleSlot> findByAvalonIdAndRoleType(Long avalonId, RoleType roleType);

    Optional<AvalonRoleSlot> findByAvalonIdAndRoleTypeForUpdate(Long avalonId, RoleType roleType);

    Optional<AvalonRoleSlot> findByAvalonIdAndSlotKey(Long avalonId, String slotKey);

    Optional<AvalonRoleSlot> findByAvalonIdAndSlotKeyForUpdate(Long avalonId, String slotKey);

    void deleteByAvalonId(Long avalonId);
}
