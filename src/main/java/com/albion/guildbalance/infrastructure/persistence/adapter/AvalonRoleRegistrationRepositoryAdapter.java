package com.albion.guildbalance.infrastructure.persistence.adapter;

import com.albion.guildbalance.application.port.AvalonRoleRegistrationRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRoleRegistration;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonRoleRegistrationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AvalonRoleRegistrationRepositoryAdapter implements AvalonRoleRegistrationRepositoryPort {

    private final AvalonRoleRegistrationJpaRepository repository;

    @Override
    public AvalonRoleRegistration save(AvalonRoleRegistration registration) {
        return repository.save(registration);
    }

    @Override
    public List<AvalonRoleRegistration> findActiveByAvalonId(Long avalonId) {
        return repository.findByAvalonRun_IdAndStatus(avalonId, RegistrationStatus.ACTIVE);
    }

    @Override
    public List<AvalonRoleRegistration> findActiveByAvalonIdAndRoleType(Long avalonId, RoleType roleType) {
        return repository.findByAvalonRun_IdAndRoleTypeAndStatus(avalonId, roleType, RegistrationStatus.ACTIVE);
    }

    @Override
    public Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerId(Long avalonId, Long playerId) {
        return repository.findByAvalonRun_IdAndPlayer_IdAndStatus(avalonId, playerId, RegistrationStatus.ACTIVE);
    }

    @Override
    public Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerIdAndRoleType(
            Long avalonId, Long playerId, RoleType roleType) {
        return repository.findByAvalonRun_IdAndPlayer_IdAndRoleTypeAndStatus(
                avalonId, playerId, roleType, RegistrationStatus.ACTIVE);
    }

    @Override
    public Optional<AvalonRoleRegistration> findActiveByAvalonIdAndPlayerIdAndSlotKey(
            Long avalonId, Long playerId, String slotKey) {
        return repository.findByAvalonRun_IdAndPlayer_IdAndSlotKeyAndStatus(
                avalonId, playerId, slotKey, RegistrationStatus.ACTIVE);
    }
}
