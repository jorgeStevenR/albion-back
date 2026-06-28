package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.AvalonRoleSlotRequest;
import com.albion.guildbalance.application.dto.request.ConfigureAvalonRolesRequest;
import com.albion.guildbalance.application.dto.request.PingTemplateRoleSlotRequest;
import com.albion.guildbalance.application.dto.request.RoleBuildSlotRequest;
import com.albion.guildbalance.application.dto.request.SwapItemRequest;
import com.albion.guildbalance.application.dto.request.UpdateRoleSlotRequest;
import com.albion.guildbalance.application.dto.response.AvalonRolePlayerResponse;
import com.albion.guildbalance.application.dto.response.AvalonRoleSlotResponse;
import com.albion.guildbalance.application.dto.response.AvalonRolesOverviewResponse;
import com.albion.guildbalance.application.dto.response.RoleBuildSlotResponse;
import com.albion.guildbalance.application.dto.response.RoleBuildTemplateResponse;
import com.albion.guildbalance.application.dto.response.SwapItemResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.exception.RoleFullException;
import com.albion.guildbalance.application.port.AvalonRoleRegistrationRepositoryPort;
import com.albion.guildbalance.application.port.AvalonRoleSlotRepositoryPort;
import com.albion.guildbalance.application.port.AvalonRunRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRoleRegistration;
import com.albion.guildbalance.domain.entity.AvalonRoleSlot;
import com.albion.guildbalance.domain.entity.AvalonRun;
import com.albion.guildbalance.domain.entity.AvalonSlotBuildItem;
import com.albion.guildbalance.domain.entity.AvalonSlotSwapItem;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import com.albion.guildbalance.domain.enums.EquipmentSlot;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.domain.util.ItemSlotClassifier;
import com.albion.guildbalance.web.security.PlayerPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvalonRoleService {

    private static final Map<RoleType, Integer> DEFAULT_SLOTS = Map.of(
            RoleType.CALLER, 1,
            RoleType.TANK, 1,
            RoleType.HEALER, 1,
            RoleType.DPS, 5,
            RoleType.SCOUT, 1,
            RoleType.SUPPORT, 0
    );

    private final AvalonRunRepositoryPort avalonRunRepository;
    private final AvalonRoleSlotRepositoryPort slotRepository;
    private final AvalonRoleRegistrationRepositoryPort registrationRepository;
    private final PlayerRepositoryPort playerRepository;
    private final RoleBuildTemplateService roleBuildTemplateService;

    @Value("${albion.render-base-url:https://render.albiononline.com/v1/item}")
    private String renderBaseUrl;

    @Transactional
    public void createDefaultSlots(AvalonRun avalonRun) {
        int order = 0;
        for (Map.Entry<RoleType, Integer> entry : DEFAULT_SLOTS.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            slotRepository.save(AvalonRoleSlot.builder()
                    .avalonRun(avalonRun)
                    .roleType(entry.getKey())
                    .slotKey(entry.getKey().name())
                    .displayName(formatRoleLabel(entry.getKey()))
                    .maxPlayers(entry.getValue())
                    .currentPlayers(0)
                    .sortOrder(order++)
                    .build());
        }
    }

    @Transactional
    public void setupPartyFromTemplate(Long avalonId, List<PingTemplateRoleSlotRequest> partySlots) {
        AvalonRun avalon = getOpenAvalonOrThrow(avalonId);
        slotRepository.deleteByAvalonId(avalonId);

        for (PingTemplateRoleSlotRequest slotReq : partySlots) {
            if (slotReq.getMaxPlayers() <= 0) {
                continue;
            }
            AvalonRoleSlot slot = AvalonRoleSlot.builder()
                    .avalonRun(avalon)
                    .slotKey(slotReq.getSlotKey())
                    .displayName(slotReq.getDisplayName())
                    .maxPlayers(slotReq.getMaxPlayers())
                    .sortOrder(slotReq.getSortOrder())
                    .currentPlayers(0)
                    .build();

            addBuildItems(slot, slotReq.getBuildSlots());
            addSwapItems(slot, slotReq.getSwapItems());
            slotRepository.save(slot);
        }
    }

    @Transactional(readOnly = true)
    public AvalonRolesOverviewResponse getRoles(Long avalonId) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        Long currentPlayerId = getCurrentPlayerIdOrNull();

        List<AvalonRoleSlot> slots = slotRepository.findByAvalonId(avalonId);
        List<AvalonRoleRegistration> registrations = registrationRepository.findActiveByAvalonId(avalonId);

        Map<String, List<AvalonRoleRegistration>> bySlotKey = registrations.stream()
                .collect(Collectors.groupingBy(r -> resolveSlotKey(r)));

        Map<RoleType, RoleBuildTemplateResponse> buildTemplates = roleBuildTemplateService.findAllAsMap();

        List<AvalonRoleSlotResponse> roleResponses = slots.stream()
                .map(slot -> toSlotResponse(
                        slot,
                        bySlotKey.getOrDefault(slot.getSlotKey(), List.of()),
                        currentPlayerId,
                        slot.getRoleType() != null ? buildTemplates.get(slot.getRoleType()) : null))
                .toList();

        return AvalonRolesOverviewResponse.builder()
                .avalonId(avalonId)
                .registrationsOpen(avalon.isRegistrationsOpen())
                .avalonOpen(avalon.getStatus() == AvalonStatus.OPEN)
                .roles(roleResponses)
                .build();
    }

    @Transactional
    public AvalonRolesOverviewResponse joinRole(Long avalonId, RoleType roleType) {
        return joinSlot(avalonId, roleType.name());
    }

    @Transactional
    public AvalonRolesOverviewResponse joinSlot(Long avalonId, String slotKey) {
        Long playerId = getCurrentPlayerId();
        AvalonRun avalon = getOpenAvalonForRegistrationOrThrow(avalonId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        registrationRepository.findActiveByAvalonIdAndPlayerId(avalonId, playerId)
                .ifPresent(existing -> {
                    if (slotKey.equals(resolveSlotKey(existing))) {
                        throw new BusinessException("Ya estás inscrito en este puesto");
                    }
                    throw new BusinessException("Ya estás inscrito en otro puesto");
                });

        AvalonRoleSlot slot = slotRepository.findByAvalonIdAndSlotKeyForUpdate(avalonId, slotKey)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + slotKey));

        if (slot.getCurrentPlayers() >= slot.getMaxPlayers()) {
            throw new RoleFullException();
        }

        AvalonRoleRegistration registration = AvalonRoleRegistration.builder()
                .avalonRun(avalon)
                .player(player)
                .roleType(slot.getRoleType())
                .slotKey(slotKey)
                .status(RegistrationStatus.ACTIVE)
                .build();

        registrationRepository.save(registration);
        slot.setCurrentPlayers(slot.getCurrentPlayers() + 1);
        slotRepository.save(slot);

        log.info("Player {} joined slot {} in avalon {}", playerId, slotKey, avalonId);
        return getRoles(avalonId);
    }

    @Transactional
    public AvalonRolesOverviewResponse leaveRole(Long avalonId, RoleType roleType) {
        return leaveSlot(avalonId, roleType.name());
    }

    @Transactional
    public AvalonRolesOverviewResponse leaveSlot(Long avalonId, String slotKey) {
        Long playerId = getCurrentPlayerId();
        getOpenAvalonForRegistrationOrThrow(avalonId);

        AvalonRoleRegistration registration = registrationRepository
                .findActiveByAvalonIdAndPlayerIdAndSlotKey(avalonId, playerId, slotKey)
                .or(() -> registrationRepository.findActiveByAvalonIdAndPlayerId(avalonId, playerId)
                        .filter(r -> slotKey.equals(resolveSlotKey(r))))
                .orElseThrow(() -> new BusinessException("No estás inscrito en este puesto"));

        AvalonRoleSlot slot = slotRepository.findByAvalonIdAndSlotKeyForUpdate(avalonId, slotKey)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + slotKey));

        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        slot.setCurrentPlayers(Math.max(0, slot.getCurrentPlayers() - 1));
        slotRepository.save(slot);

        log.info("Player {} left slot {} in avalon {}", playerId, slotKey, avalonId);
        return getRoles(avalonId);
    }

    @Transactional
    public AvalonRolesOverviewResponse configureRoles(Long avalonId, ConfigureAvalonRolesRequest request) {
        AvalonRun avalon = getOpenAvalonOrThrow(avalonId);

        Map<RoleType, AvalonRoleSlot> existing = slotRepository.findByAvalonId(avalonId).stream()
                .filter(s -> s.getRoleType() != null)
                .collect(Collectors.toMap(AvalonRoleSlot::getRoleType, s -> s));

        for (AvalonRoleSlotRequest slotRequest : request.getSlots()) {
            if (slotRequest.getMaxPlayers() <= 0) {
                continue;
            }
            AvalonRoleSlot slot = existing.get(slotRequest.getRoleType());
            if (slot == null) {
                slotRepository.save(AvalonRoleSlot.builder()
                        .avalonRun(avalon)
                        .roleType(slotRequest.getRoleType())
                        .slotKey(slotRequest.getRoleType().name())
                        .displayName(formatRoleLabel(slotRequest.getRoleType()))
                        .maxPlayers(slotRequest.getMaxPlayers())
                        .currentPlayers(0)
                        .build());
            } else if (slotRequest.getMaxPlayers() < slot.getCurrentPlayers()) {
                throw new BusinessException(
                        "Cannot reduce " + slotRequest.getRoleType() + " below current registrations");
            } else {
                slot.setMaxPlayers(slotRequest.getMaxPlayers());
                slotRepository.save(slot);
            }
        }

        return getRoles(avalonId);
    }

    @Transactional
    public AvalonRolesOverviewResponse updateRoleSlot(Long avalonId, RoleType roleType, UpdateRoleSlotRequest request) {
        getOpenAvalonOrThrow(avalonId);

        AvalonRoleSlot slot = slotRepository.findByAvalonIdAndRoleTypeForUpdate(avalonId, roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role slot not found: " + roleType));

        if (request.getMaxPlayers() < slot.getCurrentPlayers()) {
            throw new BusinessException("Cannot reduce max players below current registrations");
        }

        slot.setMaxPlayers(request.getMaxPlayers());
        slotRepository.save(slot);
        return getRoles(avalonId);
    }

    @Transactional
    public AvalonRolesOverviewResponse setRegistrationsOpen(Long avalonId, boolean open) {
        AvalonRun avalon = getOpenAvalonOrThrow(avalonId);
        avalon.setRegistrationsOpen(open);
        avalonRunRepository.save(avalon);
        return getRoles(avalonId);
    }

    private void addBuildItems(AvalonRoleSlot slot, List<RoleBuildSlotRequest> buildSlots) {
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
            slot.getBuildItems().add(AvalonSlotBuildItem.builder()
                    .roleSlot(slot)
                    .equipmentSlot(bs.getEquipmentSlot())
                    .itemUniqueName(bs.getItemUniqueName())
                    .itemDisplayName(bs.getItemDisplayName())
                    .build());
        }
    }

    private void addSwapItems(AvalonRoleSlot slot, List<SwapItemRequest> swapItems) {
        if (swapItems == null) {
            return;
        }
        int order = 0;
        for (SwapItemRequest sw : swapItems) {
            slot.getSwapItems().add(AvalonSlotSwapItem.builder()
                    .roleSlot(slot)
                    .itemUniqueName(sw.getItemUniqueName())
                    .itemDisplayName(sw.getItemDisplayName())
                    .note(sw.getNote())
                    .sortOrder(sw.getSortOrder() != null ? sw.getSortOrder() : order++)
                    .build());
        }
    }

    private AvalonRoleSlotResponse toSlotResponse(
            AvalonRoleSlot slot,
            List<AvalonRoleRegistration> registrations,
            Long currentPlayerId,
            RoleBuildTemplateResponse buildTemplate) {

        List<AvalonRolePlayerResponse> players = registrations.stream()
                .map(r -> AvalonRolePlayerResponse.builder()
                        .registrationId(r.getId())
                        .playerId(r.getPlayer().getId())
                        .albionName(r.getPlayer().getAlbionName())
                        .build())
                .toList();

        Long currentRegistrationId = null;
        if (currentPlayerId != null) {
            currentRegistrationId = registrations.stream()
                    .filter(r -> r.getPlayer().getId().equals(currentPlayerId))
                    .map(AvalonRoleRegistration::getId)
                    .findFirst()
                    .orElse(null);
        }

        List<RoleBuildSlotResponse> slotBuild = slot.getBuildItems().stream()
                .map(item -> RoleBuildSlotResponse.builder()
                        .equipmentSlot(item.getEquipmentSlot())
                        .itemUniqueName(item.getItemUniqueName())
                        .itemDisplayName(item.getItemDisplayName())
                        .iconUrl(renderBaseUrl + "/" + item.getItemUniqueName() + ".png")
                        .build())
                .toList();

        List<SwapItemResponse> slotSwaps = slot.getSwapItems().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(item -> SwapItemResponse.builder()
                        .itemUniqueName(item.getItemUniqueName())
                        .itemDisplayName(item.getItemDisplayName())
                        .iconUrl(renderBaseUrl + "/" + item.getItemUniqueName() + ".png")
                        .note(item.getNote())
                        .sortOrder(item.getSortOrder())
                        .build())
                .toList();

        return AvalonRoleSlotResponse.builder()
                .slotId(slot.getId())
                .slotKey(slot.getSlotKey())
                .displayName(slot.getDisplayName())
                .roleType(slot.getRoleType())
                .sortOrder(slot.getSortOrder())
                .maxPlayers(slot.getMaxPlayers())
                .currentPlayers(slot.getCurrentPlayers())
                .players(players)
                .full(slot.getCurrentPlayers() >= slot.getMaxPlayers())
                .currentPlayerRegistrationId(currentRegistrationId)
                .buildTemplate(slotBuild.isEmpty() ? buildTemplate : null)
                .slotBuild(slotBuild)
                .slotSwaps(slotSwaps)
                .build();
    }

    private String resolveSlotKey(AvalonRoleRegistration registration) {
        if (registration.getSlotKey() != null) {
            return registration.getSlotKey();
        }
        return registration.getRoleType() != null ? registration.getRoleType().name() : "";
    }

    private String formatRoleLabel(RoleType roleType) {
        return switch (roleType) {
            case CALLER -> "Caller";
            case TANK -> "Tank";
            case HEALER -> "Healer";
            case DPS -> "DPS";
            case SUPPORT -> "Support";
            case SCOUT -> "Scout";
        };
    }

    private AvalonRun getAvalonOrThrow(Long id) {
        return avalonRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avalon run not found with id: " + id));
    }

    private AvalonRun getOpenAvalonOrThrow(Long id) {
        AvalonRun avalon = getAvalonOrThrow(id);
        if (avalon.getStatus() != AvalonStatus.OPEN) {
            throw new BusinessException("Avalon run is already closed");
        }
        return avalon;
    }

    private AvalonRun getOpenAvalonForRegistrationOrThrow(Long id) {
        AvalonRun avalon = getOpenAvalonOrThrow(id);
        if (!avalon.isRegistrationsOpen()) {
            throw new BusinessException("Role registrations are closed for this avalon");
        }
        return avalon;
    }

    private Long getCurrentPlayerId() {
        Long playerId = getCurrentPlayerIdOrNull();
        if (playerId == null) {
            throw new BusinessException("Not authenticated");
        }
        return playerId;
    }

    private Long getCurrentPlayerIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PlayerPrincipal principal) {
            return principal.getPlayerId();
        }
        return null;
    }
}
