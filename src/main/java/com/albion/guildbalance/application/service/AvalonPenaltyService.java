package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.AdminManualPenaltyRequest;
import com.albion.guildbalance.application.dto.request.AssignDelegateRequest;
import com.albion.guildbalance.application.dto.request.ManualPenaltyRequest;
import com.albion.guildbalance.application.dto.request.MassFineRequest;
import com.albion.guildbalance.application.dto.request.NoShowPenaltyRequest;
import com.albion.guildbalance.application.dto.response.AvalonDelegateResponse;
import com.albion.guildbalance.application.dto.response.AvalonPenaltyResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.port.AvalonRoleRegistrationRepositoryPort;
import com.albion.guildbalance.application.port.AvalonRunRepositoryPort;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.*;
import com.albion.guildbalance.domain.enums.PenaltyDirection;
import com.albion.guildbalance.domain.enums.PenaltyStatus;
import com.albion.guildbalance.domain.enums.PenaltyType;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonDelegateJpaRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonPenaltyJpaRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.PenaltyAppealJpaRepository;
import com.albion.guildbalance.web.security.PlayerPrincipal;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvalonPenaltyService {

    private final AvalonRunRepositoryPort avalonRunRepository;
    private final PlayerRepositoryPort playerRepository;
    private final AvalonRoleRegistrationRepositoryPort registrationRepository;
    private final AvalonPenaltyJpaRepository penaltyRepository;
    private final AvalonDelegateJpaRepository delegateRepository;
    private final PenaltyAppealJpaRepository appealRepository;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public List<AvalonPenaltyResponse> listByAvalon(Long avalonId) {
        getAvalonOrThrow(avalonId);
        return penaltyRepository.findByAvalonRunIdOrderByCreatedAtDesc(avalonId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvalonPenaltyResponse> listAllPenalties() {
        SecurityUtils.requireAdmin();
        return penaltyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AvalonPenaltyResponse applyAdminManual(AdminManualPenaltyRequest request) {
        SecurityUtils.requireAdmin();
        return applyManual(request.getAvalonId(), request);
    }

    @Transactional(readOnly = true)
    public List<AvalonPenaltyResponse> listMyPenalties() {
        Long playerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        return penaltyRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<AvalonPenaltyResponse> applyNoShow(Long avalonId, NoShowPenaltyRequest request) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManagePenalties(avalon);

        if (request.getNoShowPlayerId().equals(request.getReplacementPlayerId())) {
            throw new BusinessException("El ausente y el reemplazo deben ser jugadores distintos");
        }

        Player noShow = getPlayerOrThrow(request.getNoShowPlayerId());
        Player replacement = getPlayerOrThrow(request.getReplacementPlayerId());
        Player createdBy = getPlayerOrThrow(SecurityUtils.getCurrentPlayer().getPlayerId());

        String reason = request.getReason() != null && !request.getReason().isBlank()
                ? request.getReason()
                : "No asistió a la avaloniana — reemplazado por " + replacement.getAlbionName();

        walletService.debit(noShow, request.getAmount());
        walletService.credit(replacement, request.getAmount());

        AvalonPenalty fine = penaltyRepository.save(AvalonPenalty.builder()
                .avalonRun(avalon)
                .player(noShow)
                .amount(request.getAmount())
                .direction(PenaltyDirection.DEBIT)
                .type(PenaltyType.NO_SHOW_FINE)
                .reason(reason)
                .createdBy(createdBy)
                .relatedPlayer(replacement)
                .build());

        AvalonPenalty reward = penaltyRepository.save(AvalonPenalty.builder()
                .avalonRun(avalon)
                .player(replacement)
                .amount(request.getAmount())
                .direction(PenaltyDirection.CREDIT)
                .type(PenaltyType.REPLACEMENT_REWARD)
                .reason("Reemplazó a " + noShow.getAlbionName())
                .createdBy(createdBy)
                .relatedPlayer(noShow)
                .build());

        log.info("No-show penalty applied on avalon {}: {} fined, {} rewarded",
                avalonId, noShow.getAlbionName(), replacement.getAlbionName());

        return List.of(toResponse(fine), toResponse(reward));
    }

    @Transactional
    public List<AvalonPenaltyResponse> applyMassFine(Long avalonId, MassFineRequest request) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManagePenalties(avalon);

        List<AvalonRoleRegistration> registrations = registrationRepository.findActiveByAvalonId(avalonId);
        if (registrations.isEmpty()) {
            throw new BusinessException("No hay jugadores registrados en esta avaloniana");
        }

        Player createdBy = getPlayerOrThrow(SecurityUtils.getCurrentPlayer().getPlayerId());
        List<AvalonPenaltyResponse> results = new ArrayList<>();

        for (AvalonRoleRegistration reg : registrations) {
            Player player = reg.getPlayer();
            walletService.debit(player, request.getAmount());

            AvalonPenalty penalty = penaltyRepository.save(AvalonPenalty.builder()
                    .avalonRun(avalon)
                    .player(player)
                    .amount(request.getAmount())
                    .direction(PenaltyDirection.DEBIT)
                    .type(PenaltyType.MASS_FINE)
                    .reason(request.getReason())
                    .createdBy(createdBy)
                    .build());

            results.add(toResponse(penalty));
        }

        log.info("Mass fine applied on avalon {} to {} players", avalonId, results.size());
        return results;
    }

    @Transactional
    public AvalonPenaltyResponse applyManual(Long avalonId, ManualPenaltyRequest request) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManagePenalties(avalon);

        Player player = getPlayerOrThrow(request.getPlayerId());
        Player createdBy = getPlayerOrThrow(SecurityUtils.getCurrentPlayer().getPlayerId());

        if (request.getDirection() == PenaltyDirection.DEBIT) {
            walletService.debit(player, request.getAmount());
        } else {
            walletService.credit(player, request.getAmount());
        }

        PenaltyType type = request.getDirection() == PenaltyDirection.DEBIT
                ? PenaltyType.MANUAL_FINE
                : PenaltyType.MANUAL_REWARD;

        AvalonPenalty penalty = penaltyRepository.save(AvalonPenalty.builder()
                .avalonRun(avalon)
                .player(player)
                .amount(request.getAmount())
                .direction(request.getDirection())
                .type(type)
                .reason(request.getReason())
                .createdBy(createdBy)
                .build());

        return toResponse(penalty);
    }

    @Transactional(readOnly = true)
    public List<AvalonDelegateResponse> listDelegates(Long avalonId) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManageDelegates(avalon);

        return delegateRepository.findByAvalonRunId(avalonId).stream()
                .map(d -> AvalonDelegateResponse.builder()
                        .id(d.getId())
                        .playerId(d.getPlayer().getId())
                        .playerName(d.getPlayer().getAlbionName())
                        .assignedByName(d.getAssignedBy().getAlbionName())
                        .createdAt(d.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public AvalonDelegateResponse assignDelegate(Long avalonId, AssignDelegateRequest request) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManageDelegates(avalon);

        Player delegate = getPlayerOrThrow(request.getPlayerId());
        Player assignedBy = getPlayerOrThrow(SecurityUtils.getCurrentPlayer().getPlayerId());

        if (delegateRepository.existsByAvalonRunIdAndPlayerId(avalonId, delegate.getId())) {
            throw new BusinessException("Este jugador ya es delegado de esta avaloniana");
        }

        AvalonDelegate saved = delegateRepository.save(AvalonDelegate.builder()
                .avalonRun(avalon)
                .player(delegate)
                .assignedBy(assignedBy)
                .build());

        return AvalonDelegateResponse.builder()
                .id(saved.getId())
                .playerId(delegate.getId())
                .playerName(delegate.getAlbionName())
                .assignedByName(assignedBy.getAlbionName())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void removeDelegate(Long avalonId, Long playerId) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        assertCanManageDelegates(avalon);
        delegateRepository.deleteByAvalonRunIdAndPlayerId(avalonId, playerId);
    }

    @Transactional(readOnly = true)
    public boolean canManagePenalties(Long avalonId) {
        AvalonRun avalon = getAvalonOrThrow(avalonId);
        return hasPenaltyPermission(avalon);
    }

    void assertCanManagePenalties(AvalonRun avalon) {
        if (!hasPenaltyPermission(avalon)) {
            throw new BusinessException("No tienes permiso para gestionar multas de esta avaloniana");
        }
    }

    private void assertCanManageDelegates(AvalonRun avalon) {
        PlayerPrincipal current = SecurityUtils.getCurrentPlayer();
        if (SecurityUtils.isAdminOrOfficer()) {
            return;
        }
        if (avalon.getCreatedBy() != null && avalon.getCreatedBy().getId().equals(current.getPlayerId())) {
            return;
        }
        throw new BusinessException("Solo el creador del ping o un oficial puede gestionar delegados");
    }

    private boolean hasPenaltyPermission(AvalonRun avalon) {
        PlayerPrincipal current = SecurityUtils.getCurrentPlayer();
        return avalon.getCreatedBy() != null
                && avalon.getCreatedBy().getId().equals(current.getPlayerId());
    }

    private AvalonRun getAvalonOrThrow(Long avalonId) {
        return avalonRunRepository.findById(avalonId)
                .orElseThrow(() -> new ResourceNotFoundException("Avalon run not found with id: " + avalonId));
    }

    private Player getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
    }

    private AvalonPenaltyResponse toResponse(AvalonPenalty penalty) {
        boolean hasAppeal = appealRepository.findByPenaltyId(penalty.getId()).isPresent();
        String appealStatus = appealRepository.findByPenaltyId(penalty.getId())
                .map(a -> a.getStatus().name())
                .orElse(null);

        return AvalonPenaltyResponse.builder()
                .id(penalty.getId())
                .avalonId(penalty.getAvalonRun().getId())
                .avalonZone(penalty.getAvalonRun().getZone())
                .playerId(penalty.getPlayer().getId())
                .playerName(penalty.getPlayer().getAlbionName())
                .amount(penalty.getAmount())
                .direction(penalty.getDirection())
                .type(penalty.getType())
                .reason(penalty.getReason())
                .status(penalty.getStatus())
                .createdById(penalty.getCreatedBy().getId())
                .createdByName(penalty.getCreatedBy().getAlbionName())
                .relatedPlayerId(penalty.getRelatedPlayer() != null ? penalty.getRelatedPlayer().getId() : null)
                .relatedPlayerName(penalty.getRelatedPlayer() != null ? penalty.getRelatedPlayer().getAlbionName() : null)
                .createdAt(penalty.getCreatedAt())
                .hasAppeal(hasAppeal)
                .appealStatus(appealStatus)
                .build();
    }
}
