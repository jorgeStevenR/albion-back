package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.RoleBuildSlotRequest;
import com.albion.guildbalance.application.dto.request.SaveRoleBuildTemplateRequest;
import com.albion.guildbalance.application.dto.response.RoleBuildSlotResponse;
import com.albion.guildbalance.application.dto.response.RoleBuildTemplateResponse;
import com.albion.guildbalance.domain.entity.RoleBuildSlot;
import com.albion.guildbalance.domain.entity.RoleBuildTemplate;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.domain.util.ItemSlotClassifier;
import com.albion.guildbalance.infrastructure.persistence.repository.RoleBuildTemplateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleBuildTemplateService {

    private static final Map<RoleType, String> DEFAULT_NAMES = Map.of(
            RoleType.CALLER, "Build Caller",
            RoleType.TANK, "Build Tank",
            RoleType.HEALER, "Build Healer",
            RoleType.DPS, "Build DPS",
            RoleType.SUPPORT, "Build Support",
            RoleType.SCOUT, "Build Scout"
    );

    private final RoleBuildTemplateJpaRepository repository;

    @Value("${albion.items.render-base-url:https://render.albiononline.com/v1/item}")
    private String renderBaseUrl;

    @Transactional(readOnly = true)
    public List<RoleBuildTemplateResponse> findAll() {
        return repository.findAllByOrderByRoleTypeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleBuildTemplateResponse findByRoleType(RoleType roleType) {
        return toResponse(getOrCreateTemplate(roleType));
    }

    @Transactional(readOnly = true)
    public Map<RoleType, RoleBuildTemplateResponse> findAllAsMap() {
        ensureDefaultTemplates();
        return repository.findAllByOrderByRoleTypeAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toMap(RoleBuildTemplateResponse::getRoleType, Function.identity()));
    }

    @Transactional
    public RoleBuildTemplateResponse save(RoleType roleType, SaveRoleBuildTemplateRequest request) {
        RoleBuildTemplate template = getOrCreateTemplate(roleType);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.getSlots().clear();

        String mainhandId = request.getSlots().stream()
                .filter(s -> s.getEquipmentSlot() == EquipmentSlot.MAINHAND)
                .map(RoleBuildSlotRequest::getItemUniqueName)
                .findFirst()
                .orElse(null);
        boolean twoHanded = ItemSlotClassifier.isTwoHandedWeapon(mainhandId);

        for (RoleBuildSlotRequest slotRequest : request.getSlots()) {
            if (twoHanded && slotRequest.getEquipmentSlot() == EquipmentSlot.OFFHAND) {
                continue;
            }
            template.getSlots().add(RoleBuildSlot.builder()
                    .template(template)
                    .equipmentSlot(slotRequest.getEquipmentSlot())
                    .itemUniqueName(slotRequest.getItemUniqueName())
                    .itemDisplayName(slotRequest.getItemDisplayName())
                    .build());
        }

        return toResponse(repository.save(template));
    }

    @Transactional
    public void ensureDefaultTemplates() {
        for (RoleType roleType : RoleType.values()) {
            repository.findByRoleType(roleType).orElseGet(() -> repository.save(RoleBuildTemplate.builder()
                    .roleType(roleType)
                    .name(DEFAULT_NAMES.getOrDefault(roleType, roleType.name() + " Build"))
                    .description("Build recomendada para " + roleType.name())
                    .build()));
        }
    }

    private RoleBuildTemplate getOrCreateTemplate(RoleType roleType) {
        ensureDefaultTemplates();
        return repository.findByRoleType(roleType).orElseThrow();
    }

    private RoleBuildTemplateResponse toResponse(RoleBuildTemplate template) {
        List<RoleBuildSlotResponse> slots = template.getSlots().stream()
                .map(slot -> RoleBuildSlotResponse.builder()
                        .equipmentSlot(slot.getEquipmentSlot())
                        .itemUniqueName(slot.getItemUniqueName())
                        .itemDisplayName(slot.getItemDisplayName())
                        .iconUrl(renderBaseUrl + "/" + slot.getItemUniqueName() + ".png")
                        .build())
                .toList();

        return RoleBuildTemplateResponse.builder()
                .id(template.getId())
                .roleType(template.getRoleType())
                .name(template.getName())
                .description(template.getDescription())
                .slots(slots)
                .build();
    }
}
