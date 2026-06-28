package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.AvalonRoleSlotRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRoleSlot;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonRoleSlotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AvalonRoleSlotRepositoryAdapter implements AvalonRoleSlotRepositoryPort {

    private final AvalonRoleSlotJpaRepository repository;

    @Override
    public AvalonRoleSlot save(AvalonRoleSlot slot) {
        return repository.save(slot);
    }

    @Override
    public List<AvalonRoleSlot> findByAvalonId(Long avalonId) {
        return repository.findByAvalonRun_IdOrderBySortOrderAsc(avalonId);
    }

    @Override
    public Optional<AvalonRoleSlot> findByAvalonIdAndRoleType(Long avalonId, RoleType roleType) {
        return repository.findByAvalonRun_IdAndRoleType(avalonId, roleType);
    }

    @Override
    public Optional<AvalonRoleSlot> findByAvalonIdAndRoleTypeForUpdate(Long avalonId, RoleType roleType) {
        return repository.findByAvalonIdAndRoleTypeForUpdate(avalonId, roleType);
    }

    @Override
    public Optional<AvalonRoleSlot> findByAvalonIdAndSlotKey(Long avalonId, String slotKey) {
        return repository.findByAvalonRun_IdAndSlotKey(avalonId, slotKey);
    }

    @Override
    public Optional<AvalonRoleSlot> findByAvalonIdAndSlotKeyForUpdate(Long avalonId, String slotKey) {
        return repository.findByAvalonIdAndSlotKeyForUpdate(avalonId, slotKey);
    }

    @Override
    public void deleteByAvalonId(Long avalonId) {
        repository.deleteByAvalonRun_Id(avalonId);
    }
}
