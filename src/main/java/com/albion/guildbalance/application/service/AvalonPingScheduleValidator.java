package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.exception.BusinessException;
import com.albion.guildbalance.application.port.AvalonRunRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class AvalonPingScheduleValidator {

    private static final Duration MIN_LEAD_TIME = Duration.ofHours(1);
    private static final Duration MIN_GAP_SAME_CREATOR = Duration.ofHours(2);
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AvalonRunRepositoryPort avalonRunRepository;

    public void validate(LocalDateTime scheduledAt, Long creatorId) {
        if (scheduledAt == null) {
            throw new BusinessException("La fecha y hora del ping son obligatorias");
        }

        LocalDateTime earliestAllowed = LocalDateTime.now().plus(MIN_LEAD_TIME);
        if (scheduledAt.isBefore(earliestAllowed)) {
            throw new BusinessException("El ping debe ser al menos 1 hora después de ahora");
        }

        avalonRunRepository.findLatestScheduledByCreator(creatorId).ifPresent(latest -> {
            LocalDateTime previous = latest.getScheduledAt();
            if (previous == null) {
                return;
            }
            LocalDateTime nextAllowed = previous.plus(MIN_GAP_SAME_CREATOR);
            if (scheduledAt.isBefore(nextAllowed)) {
                throw new BusinessException(
                        "Debes esperar 2 horas entre tus pings. Tu último ping fue el "
                                + previous.format(DISPLAY)
                                + "; el siguiente puede ser desde el "
                                + nextAllowed.format(DISPLAY));
            }
        });
    }
}
