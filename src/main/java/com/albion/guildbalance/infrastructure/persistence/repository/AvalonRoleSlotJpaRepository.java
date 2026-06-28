package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.AvalonRoleSlot;
import com.albion.guildbalance.domain.enums.RoleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvalonRoleSlotJpaRepository extends JpaRepository<AvalonRoleSlot, Long> {

    @EntityGraph(attributePaths = "buildItems")
    List<AvalonRoleSlot> findByAvalonRun_IdOrderBySortOrderAsc(Long avalonId);

    Optional<AvalonRoleSlot> findByAvalonRun_IdAndRoleType(Long avalonId, RoleType roleType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvalonRoleSlot s WHERE s.avalonRun.id = :avalonId AND s.roleType = :roleType")
    Optional<AvalonRoleSlot> findByAvalonIdAndRoleTypeForUpdate(
            @Param("avalonId") Long avalonId,
            @Param("roleType") RoleType roleType);

    @EntityGraph(attributePaths = "buildItems")
    Optional<AvalonRoleSlot> findByAvalonRun_IdAndSlotKey(Long avalonId, String slotKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "buildItems")
    @Query("SELECT s FROM AvalonRoleSlot s WHERE s.avalonRun.id = :avalonId AND s.slotKey = :slotKey")
    Optional<AvalonRoleSlot> findByAvalonIdAndSlotKeyForUpdate(
            @Param("avalonId") Long avalonId,
            @Param("slotKey") String slotKey);

    void deleteByAvalonRun_Id(Long avalonId);
}
