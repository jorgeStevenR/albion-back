package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.ReviewAppealRequest;
import com.albion.guildbalance.application.dto.request.SubmitAppealRequest;
import com.albion.guildbalance.application.dto.response.PenaltyAppealResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonPenalty;
import com.albion.guildbalance.domain.entity.PenaltyAppeal;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.AppealStatus;
import com.albion.guildbalance.domain.enums.PenaltyDirection;
import com.albion.guildbalance.domain.enums.PenaltyStatus;
import com.albion.guildbalance.infrastructure.persistence.repository.PenaltyAppealJpaRepository;
import com.albion.guildbalance.infrastructure.persistence.repository.AvalonPenaltyJpaRepository;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyAppealService {

    private final PenaltyAppealJpaRepository appealRepository;
    private final AvalonPenaltyJpaRepository penaltyRepository;
    private final AvalonPenaltyService penaltyService;
    private final PlayerRepositoryPort playerRepository;
    private final WalletService walletService;

    @Transactional
    public PenaltyAppealResponse submitAppeal(Long penaltyId, SubmitAppealRequest request) {
        AvalonPenalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty not found with id: " + penaltyId));

        Long currentPlayerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        if (!penalty.getPlayer().getId().equals(currentPlayerId)) {
            throw new BusinessException("Solo puedes apelar tus propias multas");
        }
        if (penalty.getDirection() != PenaltyDirection.DEBIT) {
            throw new BusinessException("Solo se pueden apelar descuentos (multas)");
        }
        if (penalty.getStatus() == PenaltyStatus.REVERSED) {
            throw new BusinessException("Esta multa ya fue revertida");
        }
        if (appealRepository.findByPenaltyId(penaltyId).isPresent()) {
            throw new BusinessException("Ya existe una apelación para esta multa");
        }

        Player player = getPlayerOrThrow(currentPlayerId);

        PenaltyAppeal appeal = appealRepository.save(PenaltyAppeal.builder()
                .penalty(penalty)
                .player(player)
                .reason(request.getReason())
                .build());

        penalty.setStatus(PenaltyStatus.APPEAL_PENDING);
        penaltyRepository.save(penalty);

        log.info("Appeal submitted for penalty {} by {}", penaltyId, player.getAlbionName());
        return toResponse(appeal);
    }

    @Transactional(readOnly = true)
    public List<PenaltyAppealResponse> listMyAppeals() {
        Long playerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        return appealRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PenaltyAppealResponse> listAllAppeals() {
        SecurityUtils.requireAdmin();
        return appealRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PenaltyAppealResponse> listPendingAppeals() {
        if (SecurityUtils.isAdminOrOfficer()) {
            return appealRepository.findByStatusOrderByCreatedAtAsc(AppealStatus.PENDING).stream()
                    .map(this::toResponse)
                    .toList();
        }
        Long playerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        return appealRepository.findByStatusOrderByCreatedAtAsc(AppealStatus.PENDING).stream()
                .filter(a -> penaltyService.canManagePenalties(a.getPenalty().getAvalonRun().getId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PenaltyAppealResponse reviewAppeal(Long appealId, ReviewAppealRequest request) {
        PenaltyAppeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal not found with id: " + appealId));

        if (!SecurityUtils.isAdminOrOfficer()) {
            penaltyService.assertCanManagePenalties(appeal.getPenalty().getAvalonRun());
        }

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new BusinessException("Esta apelación ya fue revisada");
        }
        if (request.getDecision() == AppealStatus.PENDING) {
            throw new BusinessException("Debes aprobar o rechazar la apelación");
        }

        Player reviewer = getPlayerOrThrow(SecurityUtils.getCurrentPlayer().getPlayerId());
        AvalonPenalty penalty = appeal.getPenalty();

        appeal.setStatus(request.getDecision());
        appeal.setReviewNotes(request.getReviewNotes());
        appeal.setReviewedBy(reviewer);
        appeal.setReviewedAt(LocalDateTime.now());

        if (request.getDecision() == AppealStatus.APPROVED) {
            walletService.credit(penalty.getPlayer(), penalty.getAmount());
            penalty.setStatus(PenaltyStatus.REVERSED);
            penaltyRepository.save(penalty);
            log.info("Appeal {} approved — penalty {} reversed", appealId, penalty.getId());
        } else {
            penalty.setStatus(PenaltyStatus.APPLIED);
            penaltyRepository.save(penalty);
            log.info("Appeal {} rejected", appealId);
        }

        return toResponse(appealRepository.save(appeal));
    }

    private Player getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
    }

    private PenaltyAppealResponse toResponse(PenaltyAppeal appeal) {
        AvalonPenalty penalty = appeal.getPenalty();
        return PenaltyAppealResponse.builder()
                .id(appeal.getId())
                .penaltyId(penalty.getId())
                .avalonId(penalty.getAvalonRun().getId())
                .avalonZone(penalty.getAvalonRun().getZone())
                .playerId(appeal.getPlayer().getId())
                .playerName(appeal.getPlayer().getAlbionName())
                .amount(penalty.getAmount())
                .penaltyReason(penalty.getReason())
                .reason(appeal.getReason())
                .status(appeal.getStatus())
                .reviewNotes(appeal.getReviewNotes())
                .reviewedByName(appeal.getReviewedBy() != null ? appeal.getReviewedBy().getAlbionName() : null)
                .createdAt(appeal.getCreatedAt())
                .reviewedAt(appeal.getReviewedAt())
                .build();
    }
}
