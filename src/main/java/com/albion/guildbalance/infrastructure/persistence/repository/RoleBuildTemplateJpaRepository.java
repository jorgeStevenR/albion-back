package com.albion.guildbalance.infrastructure.persistence.repository;

import com.albion.guildbalance.domain.entity.RoleBuildTemplate;
import com.albion.guildbalance.domain.enums.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleBuildTemplateJpaRepository extends JpaRepository<RoleBuildTemplate, Long> {

    @EntityGraph(attributePaths = "slots")
    Optional<RoleBuildTemplate> findByRoleType(RoleType roleType);

    @EntityGraph(attributePaths = "slots")
    List<RoleBuildTemplate> findAllByOrderByRoleTypeAsc();
}
