package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.CreateMoneyRequestDto;
import com.albion.guildbalance.application.dto.request.ReviewMoneyRequestDto;
import com.albion.guildbalance.application.dto.response.MoneyRequestResponse;
import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.exception.ResourceNotFoundException;
import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.albion.guildbalance.application.port.WalletRepositoryPort;
import com.albion.guildbalance.domain.entity.GuildMoneyRequest;
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.entity.Wallet;
import com.albion.guildbalance.domain.enums.MoneyRequestStatus;
import com.albion.guildbalance.domain.enums.MoneyRequestType;
import com.albion.guildbalance.infrastructure.persistence.repository.GuildMoneyRequestJpaRepository;
import com.albion.guildbalance.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoneyRequestService {

    private final GuildMoneyRequestJpaRepository requestRepository;
    private final PlayerRepositoryPort playerRepository;
    private final WalletService walletService;
    private final WalletRepositoryPort walletRepository;

    @Transactional
    public MoneyRequestResponse createWithdrawal(CreateMoneyRequestDto dto) {
        return create(dto, MoneyRequestType.WITHDRAWAL);
    }

    @Transactional
    public MoneyRequestResponse createLoan(CreateMoneyRequestDto dto) {
        return create(dto, MoneyRequestType.LOAN);
    }

    @Transactional(readOnly = true)
    public List<MoneyRequestResponse> myWithdrawals() {
        return listMine(MoneyRequestType.WITHDRAWAL);
    }

    @Transactional(readOnly = true)
    public List<MoneyRequestResponse> myLoans() {
        return listMine(MoneyRequestType.LOAN);
    }

    @Transactional(readOnly = true)
    public List<MoneyRequestResponse> allWithdrawals() {
        SecurityUtils.requireAdmin();
        return requestRepository.findByTypeOrderByCreatedAtDesc(MoneyRequestType.WITHDRAWAL).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MoneyRequestResponse> allLoans() {
        SecurityUtils.requireAdmin();
        return requestRepository.findByTypeOrderByCreatedAtDesc(MoneyRequestType.LOAN).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MoneyRequestResponse review(Long id, ReviewMoneyRequestDto dto) {
        SecurityUtils.requireAdmin();
        GuildMoneyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (request.getStatus() != MoneyRequestStatus.PENDING) {
            throw new BusinessException("Solo se pueden revisar solicitudes pendientes");
        }

        MoneyRequestStatus newStatus = dto.getStatus();
        if (newStatus != MoneyRequestStatus.APPROVED
                && newStatus != MoneyRequestStatus.REJECTED
                && newStatus != MoneyRequestStatus.ACTIVE) {
            throw new BusinessException("Estado de revisión no válido");
        }

        Player reviewer = playerRepository.findById(SecurityUtils.getCurrentPlayer().getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        if (request.getPlayer().getId().equals(reviewer.getId())) {
            throw new BusinessException("No puedes revisar tus propias solicitudes");
        }

        if (newStatus == MoneyRequestStatus.REJECTED) {
            request.setStatus(MoneyRequestStatus.REJECTED);
        } else if (request.getType() == MoneyRequestType.WITHDRAWAL) {
            if (newStatus != MoneyRequestStatus.APPROVED) {
                throw new BusinessException("Los adelantos solo pueden aprobarse o rechazarse");
            }
            ensureSufficientBalance(request.getPlayer(), request.getAmount());
            walletService.debit(request.getPlayer(), request.getAmount());
            request.setStatus(MoneyRequestStatus.APPROVED);
        } else {
            if (newStatus != MoneyRequestStatus.ACTIVE) {
                throw new BusinessException("Los préstamos solo pueden activarse o rechazarse");
            }
            walletService.credit(request.getPlayer(), request.getAmount());
            request.setStatus(MoneyRequestStatus.ACTIVE);
        }

        request.setReviewedBy(reviewer);
        request.setReviewNotes(dto.getReviewNotes());
        request.setReviewedAt(LocalDateTime.now());
        return toResponse(requestRepository.save(request));
    }

    private MoneyRequestResponse create(CreateMoneyRequestDto dto, MoneyRequestType type) {
        if (SecurityUtils.isAdmin()) {
            throw new BusinessException("Los administradores no pueden solicitar adelantos ni préstamos");
        }

        Player player = playerRepository.findById(SecurityUtils.getCurrentPlayer().getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        GuildMoneyRequest request = GuildMoneyRequest.builder()
                .player(player)
                .type(type)
                .amount(dto.getAmount())
                .reason(dto.getReason().trim())
                .status(MoneyRequestStatus.PENDING)
                .build();

        return toResponse(requestRepository.save(request));
    }

    private List<MoneyRequestResponse> listMine(MoneyRequestType type) {
        Long playerId = SecurityUtils.getCurrentPlayer().getPlayerId();
        return requestRepository.findByPlayerIdAndTypeOrderByCreatedAtDesc(playerId, type).stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureSufficientBalance(Player player, BigDecimal amount) {
        Wallet wallet = walletRepository.findByPlayerId(player.getId())
                .orElse(null);
        BigDecimal balance = wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(amount) < 0) {
            throw new BusinessException("El jugador no tiene balance suficiente para este adelanto");
        }
    }

    private MoneyRequestResponse toResponse(GuildMoneyRequest request) {
        return MoneyRequestResponse.builder()
                .id(request.getId())
                .playerId(request.getPlayer().getId())
                .playerName(request.getPlayer().getAlbionName())
                .type(request.getType())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(request.getStatus())
                .reviewedByName(request.getReviewedBy() != null ? request.getReviewedBy().getAlbionName() : null)
                .reviewNotes(request.getReviewNotes())
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }
}
