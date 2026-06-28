package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.AvalonMapsRequest;
import com.albion.guildbalance.application.dto.request.AvalonRunRequest;
import com.albion.guildbalance.application.dto.request.BagGrossRequest;
import com.albion.guildbalance.application.dto.request.LootItemRequest;
import com.albion.guildbalance.application.dto.request.ParticipantRequest;
import com.albion.guildbalance.application.dto.response.AvalonRunResponse;
import com.albion.guildbalance.application.dto.response.DistributionCalculationResponse;
import com.albion.guildbalance.application.dto.response.DistributionResponse;
import com.albion.guildbalance.application.dto.response.ParticipantResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.mapper.EntityMapper;
import com.albion.guildbalance.application.port.*;
import com.albion.guildbalance.domain.entity.*;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import com.albion.guildbalance.domain.enums.LootSaleStatus;
import com.albion.guildbalance.domain.enums.LootType;
import com.albion.guildbalance.domain.enums.ParticipantType;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.service.BalanceCalculator;
import com.albion.guildbalance.infrastructure.persistence.repository.LootItemJpaRepository;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvalonRunService {

    private final AvalonRunRepositoryPort avalonRunRepository;
    private final PlayerRepositoryPort playerRepository;
    private final DistributionRepositoryPort distributionRepository;
    private final WalletRepositoryPort walletRepository;
    private final AvalonRoleService avalonRoleService;
    private final AvalonRoleRegistrationRepositoryPort registrationRepository;
    private final AvalonRoleSlotRepositoryPort slotRepository;
    private final LootItemJpaRepository lootItemRepository;
    private final EntityMapper mapper;
    private final AvalonPingScheduleValidator pingScheduleValidator;

    @Transactional(readOnly = true)
    public List<AvalonRunResponse> findAll() {
        log.debug("Fetching all avalon runs");
        List<AvalonRunResponse> responses = mapper.toAvalonRunResponseList(avalonRunRepository.findAll());
        responses.forEach(this::enrichRegistrationCounts);
        return responses;
    }

    @Transactional
    public AvalonRunResponse findById(Long id) {
        AvalonRun avalon = getAvalonOrThrow(id);
        if (syncParticipantsFromRegistrations(avalon)) {
            avalon = avalonRunRepository.save(avalon);
        }
        AvalonRunResponse response = mapper.toAvalonRunResponse(avalon);
        enrichRegistrationCounts(response);
        enrichParticipantRoles(response, id);
        return response;
    }

    @Transactional
    public AvalonRunResponse create(AvalonRunRequest request) {
        log.info("Creating avalon run in zone: {}", request.getZone());
        Player creator = playerRepository.findById(SecurityUtils.getCurrentPlayer().getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        pingScheduleValidator.validate(request.getScheduledAt(), creator.getId());

        AvalonRun avalonRun = AvalonRun.builder()
                .date(request.getDate())
                .scheduledAt(request.getScheduledAt())
                .zone(request.getZone())
                .description(request.getDescription())
                .status(AvalonStatus.OPEN)
                .registrationsOpen(true)
                .createdBy(creator)
                .build();

        AvalonRun saved = avalonRunRepository.save(avalonRun);
        avalonRoleService.createDefaultSlots(saved);
        return mapper.toAvalonRunResponse(saved);
    }

    @Transactional
    public AvalonRunResponse addParticipant(Long avalonId, ParticipantRequest request) {
        log.info("Adding participant {} to avalon {}", request.getPlayerId(), avalonId);
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);
        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + request.getPlayerId()));

        boolean alreadyParticipant = avalonRun.getParticipants().stream()
                .anyMatch(p -> p.getPlayer().getId().equals(player.getId()));
        if (alreadyParticipant) {
            throw new BusinessException("Player is already a participant in this avalon run");
        }

        AvalonParticipant participant = AvalonParticipant.builder()
                .avalonRun(avalonRun)
                .player(player)
                .participantType(request.getParticipantType())
                .build();

        avalonRun.getParticipants().add(participant);
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    @Transactional
    public AvalonRunResponse addLoot(Long avalonId, LootItemRequest request) {
        log.info("Adding loot '{}' to avalon {}", request.getName(), avalonId);
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);

        LootItem lootItem = LootItem.builder()
                .avalonRun(avalonRun)
                .name(request.getName())
                .type(request.getType())
                .quantity(request.getQuantity())
                .marketValue(request.getMarketValue())
                .saleStatus(request.getType() == LootType.BAG
                        ? LootSaleStatus.NOT_APPLICABLE
                        : LootSaleStatus.UNSOLD)
                .build();

        avalonRun.getLootItems().add(lootItem);
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    @Transactional
    public AvalonRunResponse setBagGross(Long avalonId, BagGrossRequest request) {
        log.info("Setting bag gross for avalon {}: {}", avalonId, request.getGrossValue());
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);
        avalonRun.getLootItems().removeIf(l -> l.getType() == LootType.BAG);
        if (request.getGrossValue().compareTo(BigDecimal.ZERO) > 0) {
            avalonRun.getLootItems().add(LootItem.builder()
                    .avalonRun(avalonRun)
                    .name("Bolsitas del piso")
                    .type(LootType.BAG)
                    .quantity(1)
                    .marketValue(request.getGrossValue())
                    .saleStatus(LootSaleStatus.NOT_APPLICABLE)
                    .build());
        }
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    @Transactional
    public AvalonRunResponse addChest(Long avalonId, BagGrossRequest request) {
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);
        long chestNumber = avalonRun.getLootItems().stream()
                .filter(l -> l.getType() == LootType.ITEM)
                .count() + 1;

        LootItem lootItem = LootItem.builder()
                .avalonRun(avalonRun)
                .name("Cofre " + chestNumber)
                .type(LootType.ITEM)
                .quantity(1)
                .marketValue(request.getGrossValue())
                .saleStatus(LootSaleStatus.UNSOLD)
                .build();

        avalonRun.getLootItems().add(lootItem);
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    @Transactional
    public AvalonRunResponse updateMaps(Long avalonId, AvalonMapsRequest request) {
        log.info("Updating maps for avalon {}: {} maps, cost {}", avalonId, request.getMapsThrown(), request.getMapsCost());
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);
        avalonRun.setMapsThrown(request.getMapsThrown());
        avalonRun.setMapsCost(request.getMapsCost());
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    @Transactional
    public DistributionCalculationResponse calculateAndFinish(Long avalonId) {
        log.info("Calculating distribution and finishing avalon {}", avalonId);
        AvalonRun avalonRun = getOpenAvalonOrThrow(avalonId);

        if (syncParticipantsFromRegistrations(avalonRun)) {
            avalonRun = avalonRunRepository.save(avalonRun);
        }

        if (avalonRun.getParticipants().isEmpty()) {
            throw new BusinessException("No se puede calcular el reparto sin participantes inscritos");
        }
        if (avalonRun.getLootItems().isEmpty()) {
            throw new BusinessException("No se puede calcular el reparto sin loot");
        }

        BigDecimal totalBalance = BalanceCalculator.calculateTotalBalance(
                avalonRun.getLootItems(), avalonRun.getMapsCost());
        double totalWeight = BalanceCalculator.calculateTotalWeight(avalonRun.getParticipants());

        List<Distribution> distributions = new ArrayList<>();
        for (AvalonParticipant participant : avalonRun.getParticipants()) {
            BigDecimal amount = BalanceCalculator.calculateParticipantShare(
                    totalBalance, totalWeight, participant.getParticipantType());

            Distribution distribution = Distribution.builder()
                    .avalonRun(avalonRun)
                    .player(participant.getPlayer())
                    .amount(amount)
                    .build();

            distributions.add(distributionRepository.save(distribution));
            creditWallet(participant.getPlayer(), amount);
        }

        avalonRun.setStatus(AvalonStatus.FINISHED);
        avalonRun.setRegistrationsOpen(false);
        avalonRunRepository.save(avalonRun);

        List<DistributionResponse> distributionResponses = mapper.toDistributionResponseList(distributions);

        return DistributionCalculationResponse.builder()
                .avalonId(avalonId)
                .totalBalance(totalBalance)
                .totalWeight(totalWeight)
                .distributions(distributionResponses)
                .build();
    }

    @Transactional
    public AvalonRunResponse closeAvalon(Long avalonId) {
        AvalonRun avalonRun = getAvalonOrThrow(avalonId);
        if (avalonRun.getStatus() != AvalonStatus.FINISHED) {
            throw new BusinessException("Solo se puede cerrar una avaloniana terminada");
        }

        long unsoldItems = lootItemRepository.countByAvalonRunIdAndSaleStatus(avalonId, LootSaleStatus.UNSOLD);
        if (unsoldItems > 0) {
            throw new BusinessException(
                    "No se puede cerrar: quedan " + unsoldItems + " items sin vender. Vende todo el loot primero.");
        }

        avalonRun.setStatus(AvalonStatus.CLOSED);
        return mapper.toAvalonRunResponse(avalonRunRepository.save(avalonRun));
    }

    /** @deprecated use {@link #calculateAndFinish(Long)} */
    @Transactional
    public DistributionCalculationResponse calculateAndClose(Long avalonId) {
        return calculateAndFinish(avalonId);
    }

    private void creditWallet(Player player, BigDecimal amount) {
        Wallet wallet = walletRepository.findByPlayerId(player.getId())
                .orElseGet(() -> Wallet.builder().player(player).balance(BigDecimal.ZERO).build());
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    private AvalonRun getAvalonOrThrow(Long id) {
        return avalonRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avalon run not found with id: " + id));
    }

    private AvalonRun getOpenAvalonOrThrow(Long id) {
        AvalonRun avalonRun = getAvalonOrThrow(id);
        if (avalonRun.getStatus() != AvalonStatus.OPEN) {
            throw new BusinessException("La avaloniana ya no está abierta");
        }
        return avalonRun;
    }

    private boolean syncParticipantsFromRegistrations(AvalonRun avalon) {
        List<AvalonRoleRegistration> registrations = registrationRepository.findActiveByAvalonId(avalon.getId());
        boolean changed = false;
        for (AvalonRoleRegistration registration : registrations) {
            Long playerId = registration.getPlayer().getId();
            boolean exists = avalon.getParticipants().stream()
                    .anyMatch(p -> p.getPlayer().getId().equals(playerId));
            if (!exists) {
                avalon.getParticipants().add(AvalonParticipant.builder()
                        .avalonRun(avalon)
                        .player(registration.getPlayer())
                        .participantType(resolveParticipantType(registration))
                        .build());
                changed = true;
            }
        }
        return changed;
    }

    private void enrichRegistrationCounts(AvalonRunResponse response) {
        response.setRegisteredCount((int) registrationRepository.countActiveByAvalonId(response.getId()));
        response.setTotalCapacity(slotRepository.findByAvalonId(response.getId()).stream()
                .mapToInt(AvalonRoleSlot::getMaxPlayers)
                .sum());
    }

    private void enrichParticipantRoles(AvalonRunResponse response, Long avalonId) {
        if (response.getParticipants() == null) {
            return;
        }
        List<AvalonRoleRegistration> registrations = registrationRepository.findActiveByAvalonId(avalonId);
        Map<Long, AvalonRoleRegistration> byPlayer = registrations.stream()
                .collect(Collectors.toMap(r -> r.getPlayer().getId(), r -> r, (a, b) -> a));
        Map<String, String> slotNames = slotRepository.findByAvalonId(avalonId).stream()
                .collect(Collectors.toMap(AvalonRoleSlot::getSlotKey, AvalonRoleSlot::getDisplayName, (a, b) -> a));

        for (ParticipantResponse participant : response.getParticipants()) {
            AvalonRoleRegistration registration = byPlayer.get(participant.getPlayerId());
            if (registration == null) {
                continue;
            }
            String slotKey = registration.getSlotKey() != null
                    ? registration.getSlotKey()
                    : (registration.getRoleType() != null ? registration.getRoleType().name() : null);
            participant.setRoleSlotKey(slotKey);
            participant.setRoleDisplayName(slotKey != null ? slotNames.getOrDefault(slotKey, slotKey) : null);
        }
    }

    private ParticipantType resolveParticipantType(AvalonRoleRegistration registration) {
        String slotKey = registration.getSlotKey();
        if (slotKey != null && (slotKey.equals("SCAUT") || slotKey.equals("SCOUT"))) {
            return ParticipantType.SCOUT;
        }
        return ParticipantType.PLAYER;
    }
}
