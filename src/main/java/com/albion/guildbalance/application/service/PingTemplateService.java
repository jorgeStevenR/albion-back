package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.CreateAvalonFromTemplateRequest;
import com.albion.guildbalance.application.dto.request.AvalonRunRequest;
import com.albion.guildbalance.application.dto.request.RoleBuildSlotRequest;
import com.albion.guildbalance.application.dto.request.SavePingTemplateRequest;
import com.albion.guildbalance.application.dto.request.SwapItemRequest;
import com.albion.guildbalance.application.dto.response.PingTemplateResponse;
import com.albion.guildbalance.application.dto.response.PingTemplateRoleSlotResponse;
import com.albion.guildbalance.application.dto.response.RoleBuildSlotResponse;
import com.albion.guildbalance.application.dto.response.SwapItemResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonPingTemplate;
import com.albion.guildbalance.domain.entity.PingTemplateBuildSlot;
import com.albion.guildbalance.domain.entity.PingTemplateRoleSlot;
import com.albion.guildbalance.domain.entity.PingTemplateSwapItem;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import com.albion.guildbalance.domain.util.ItemSlotClassifier;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonPingTemplateJpaRepository;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PingTemplateService {

    private final AvalonPingTemplateJpaRepository templateRepository;
    private final PlayerRepositoryPort playerRepository;
    private final AvalonRunService avalonRunService;
    private final AvalonRoleService avalonRoleService;

    @Value("${albion.render-base-url:https://render.albiononline.com/v1/item}")
    private String renderBaseUrl;

    @Transactional(readOnly = true)
    public List<PingTemplateResponse> findAllActive() {
        return templateRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toMinimalResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PingTemplateResponse> findAll() {
        return templateRepository.findAllByOrderByNameAsc().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional
    public PingTemplateResponse create(SavePingTemplateRequest request) {
        Player creator = playerRepository.findById(SecurityUtils.getCurrentPlayer().getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        AvalonPingTemplate template = AvalonPingTemplate.builder()
                .name(request.getName())
                .zone(request.getZone())
                .description(request.getDescription())
                .pingMessage(request.getPingMessage())
                .createdBy(creator)
                .build();

        for (var slotReq : request.getRoleSlots()) {
            if (slotReq.getMaxPlayers() <= 0) {
                continue;
            }
            PingTemplateRoleSlot partySlot = PingTemplateRoleSlot.builder()
                    .template(template)
                    .slotKey(slotReq.getSlotKey())
                    .displayName(slotReq.getDisplayName())
                    .maxPlayers(slotReq.getMaxPlayers())
                    .sortOrder(slotReq.getSortOrder())
                    .build();
            addBuildSlots(partySlot, slotReq.getBuildSlots());
            addSwapItems(partySlot, slotReq.getSwapItems());
            template.getRoleSlots().add(partySlot);
        }

        return toFullResponse(templateRepository.save(template));
    }

    @Transactional
    public void deactivate(Long id) {
        AvalonPingTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        template.setActive(false);
        templateRepository.save(template);
    }

    @Transactional
    public Long createAvalonFromTemplate(Long templateId, CreateAvalonFromTemplateRequest request) {
        AvalonPingTemplate template = templateRepository.findWithSlotsById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        for (PingTemplateRoleSlot slot : template.getRoleSlots()) {
            Hibernate.initialize(slot.getBuildSlots());
        }
        for (PingTemplateRoleSlot slot : template.getRoleSlots()) {
            Hibernate.initialize(slot.getSwapItems());
        }

        LocalDateTime scheduledAt = request != null ? request.getScheduledAt() : null;
        if (scheduledAt == null) {
            throw new BusinessException("La fecha y hora del ping son obligatorias");
        }
        LocalDate date = scheduledAt.toLocalDate();

        var avalonResponse = avalonRunService.create(AvalonRunRequest.builder()
                .date(date)
                .scheduledAt(scheduledAt)
                .zone(template.getZone())
                .description(template.getDescription())
                .build());

        List<com.albion.guildbalance.application.dto.request.PingTemplateRoleSlotRequest> slots =
                template.getRoleSlots().stream()
                        .map(s -> {
                            var req = new com.albion.guildbalance.application.dto.request.PingTemplateRoleSlotRequest();
                            req.setSlotKey(s.getSlotKey());
                            req.setDisplayName(s.getDisplayName());
                            req.setMaxPlayers(s.getMaxPlayers());
                            req.setSortOrder(s.getSortOrder());
                            req.setBuildSlots(s.getBuildSlots().stream()
                                    .map(b -> RoleBuildSlotRequest.builder()
                                            .equipmentSlot(b.getEquipmentSlot())
                                            .itemUniqueName(b.getItemUniqueName())
                                            .itemDisplayName(b.getItemDisplayName())
                                            .build())
                                    .toList());
                            req.setSwapItems(s.getSwapItems().stream()
                                    .map(sw -> SwapItemRequest.builder()
                                            .itemUniqueName(sw.getItemUniqueName())
                                            .itemDisplayName(sw.getItemDisplayName())
                                            .note(sw.getNote())
                                            .sortOrder(sw.getSortOrder())
                                            .build())
                                    .toList());
                            return req;
                        })
                        .toList();

        avalonRoleService.setupPartyFromTemplate(avalonResponse.getId(), slots);
        return avalonResponse.getId();
    }

    private void addBuildSlots(PingTemplateRoleSlot partySlot, List<RoleBuildSlotRequest> buildSlots) {
        if (buildSlots == null) {
            return;
        }
        boolean twoHand = buildSlots.stream()
                .anyMatch(b -> b.getEquipmentSlot() == EquipmentSlot.MAINHAND
                        && ItemSlotClassifier.isTwoHandedWeapon(b.getItemUniqueName()));

        for (RoleBuildSlotRequest bs : buildSlots) {
            if (twoHand && bs.getEquipmentSlot() == EquipmentSlot.OFFHAND) {
                continue;
            }
            partySlot.getBuildSlots().add(PingTemplateBuildSlot.builder()
                    .partySlot(partySlot)
                    .equipmentSlot(bs.getEquipmentSlot())
                    .itemUniqueName(bs.getItemUniqueName())
                    .itemDisplayName(bs.getItemDisplayName())
                    .build());
        }
    }

    private void addSwapItems(PingTemplateRoleSlot partySlot, List<SwapItemRequest> swapItems) {
        if (swapItems == null) {
            return;
        }
        int order = 0;
        for (SwapItemRequest sw : swapItems) {
            partySlot.getSwapItems().add(PingTemplateSwapItem.builder()
                    .partySlot(partySlot)
                    .itemUniqueName(sw.getItemUniqueName())
                    .itemDisplayName(sw.getItemDisplayName())
                    .note(sw.getNote())
                    .sortOrder(sw.getSortOrder() != null ? sw.getSortOrder() : order++)
                    .build());
        }
    }

    private SwapItemResponse toSwapResponse(PingTemplateSwapItem item) {
        return SwapItemResponse.builder()
                .itemUniqueName(item.getItemUniqueName())
                .itemDisplayName(item.getItemDisplayName())
                .iconUrl(renderBaseUrl + "/" + item.getItemUniqueName() + ".png")
                .note(item.getNote())
                .sortOrder(item.getSortOrder())
                .build();
    }

    private PingTemplateResponse toMinimalResponse(AvalonPingTemplate template) {
        return PingTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .zone(template.getZone())
                .description(template.getDescription())
                .pingMessage(template.getPingMessage())
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .roleSlots(List.of())
                .build();
    }

    private PingTemplateResponse toSummaryResponse(AvalonPingTemplate template) {
        Hibernate.initialize(template.getRoleSlots());
        return PingTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .zone(template.getZone())
                .description(template.getDescription())
                .pingMessage(template.getPingMessage())
                .active(template.isActive())
                .createdByName(template.getCreatedBy() != null ? template.getCreatedBy().getAlbionName() : null)
                .createdAt(template.getCreatedAt())
                .roleSlots(template.getRoleSlots().stream()
                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(s -> PingTemplateRoleSlotResponse.builder()
                                .slotKey(s.getSlotKey())
                                .displayName(s.getDisplayName())
                                .maxPlayers(s.getMaxPlayers())
                                .sortOrder(s.getSortOrder())
                                .buildSlots(List.of())
                                .swapItems(List.of())
                                .build())
                        .toList())
                .build();
    }

    private PingTemplateResponse toFullResponse(AvalonPingTemplate template) {
        return PingTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .zone(template.getZone())
                .description(template.getDescription())
                .pingMessage(template.getPingMessage())
                .active(template.isActive())
                .createdByName(template.getCreatedBy() != null ? template.getCreatedBy().getAlbionName() : null)
                .createdAt(template.getCreatedAt())
                .roleSlots(template.getRoleSlots().stream()
                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(s -> PingTemplateRoleSlotResponse.builder()
                                .slotKey(s.getSlotKey())
                                .displayName(s.getDisplayName())
                                .maxPlayers(s.getMaxPlayers())
                                .sortOrder(s.getSortOrder())
                                .buildSlots(s.getBuildSlots().stream()
                                        .map(b -> RoleBuildSlotResponse.builder()
                                                .equipmentSlot(b.getEquipmentSlot())
                                                .itemUniqueName(b.getItemUniqueName())
                                                .itemDisplayName(b.getItemDisplayName())
                                                .iconUrl(renderBaseUrl + "/" + b.getItemUniqueName() + ".png")
                                                .build())
                                        .toList())
                                .swapItems(s.getSwapItems().stream()
                                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                                        .map(this::toSwapResponse)
                                        .toList())
                                .build())
                        .toList())
                .build();
    }
}
