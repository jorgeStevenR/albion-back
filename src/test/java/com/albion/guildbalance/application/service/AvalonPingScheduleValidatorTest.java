package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.port.AvalonRunRepositoryPort;
import com.albion.guildbalance.domain.entity.AvalonRun;
import com.albion.guildbalance.domain.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvalonPingScheduleValidatorTest {

    @Mock
    private AvalonRunRepositoryPort avalonRunRepository;

    @InjectMocks
    private AvalonPingScheduleValidator validator;

    @Test
    void rejectsPingLessThanOneHourAhead() {
        assertThrows(Exception.class, () ->
                validator.validate(LocalDateTime.now().plusMinutes(30), 1L));
    }

    @Test
    void rejectsSecondPingWithinOneHourForSameCreator() {
        LocalDateTime previous = LocalDateTime.now().plusHours(3);
        when(avalonRunRepository.findLatestScheduledByCreator(1L)).thenReturn(Optional.of(
                AvalonRun.builder().scheduledAt(previous).createdBy(Player.builder().id(1L).build()).build()
        ));

        assertThrows(Exception.class, () ->
                validator.validate(previous.plusMinutes(30), 1L));
    }

    @Test
    void allowsSecondPingAfterOneHourForSameCreator() {
        LocalDateTime previous = LocalDateTime.now().plusHours(3);
        when(avalonRunRepository.findLatestScheduledByCreator(1L)).thenReturn(Optional.of(
                AvalonRun.builder().scheduledAt(previous).build()
        ));

        assertDoesNotThrow(() -> validator.validate(previous.plusHours(1), 1L));
    }
}
