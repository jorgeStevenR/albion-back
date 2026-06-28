package com.albion.guildbalance.application.service;

import com.albion.guildbalance.application.dto.request.ConfigureAvalonRolesRequest;
import com.albion.guildbalance.application.dto.request.AvalonRoleSlotRequest;
import com.albion.guildbalance.application.dto.response.AvalonRolesOverviewResponse;
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
import com.albion.guildbalance.domain.entity.Player;
import com.albion.guildbalance.domain.enums.AvalonStatus;
import com.albion.guildbalance.domain.enums.RegistrationStatus;
import com.albion.guildbalance.domain.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import com.albion.guildbalance.web.security.PlayerPrincipal;
import com.albion.guildbalance.domain.enums.PlayerRole;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvalonRoleServiceTest {

    @Mock
    private AvalonRunRepositoryPort avalonRunRepository;
    @Mock
    private AvalonRoleSlotRepositoryPort slotRepository;
    @Mock
    private AvalonRoleRegistrationRepositoryPort registrationRepository;
    @Mock
    private PlayerRepositoryPort playerRepository;
    @Mock
    private RoleBuildTemplateService roleBuildTemplateService;

    @InjectMocks
    private AvalonRoleService avalonRoleService;

    private final AvalonRun openAvalon = AvalonRun.builder()
            .id(1L)
            .date(LocalDate.now())
            .zone("T7")
            .status(AvalonStatus.OPEN)
            .registrationsOpen(true)
            .build();

    @Test
    @DisplayName("Join role succeeds when slot has capacity")
    void joinRole_success() {
        setCurrentPlayer(10L);
        AvalonRoleSlot slot = AvalonRoleSlot.builder()
                .id(1L).avalonRun(openAvalon).roleType(RoleType.CALLER)
                .slotKey("CALLER").displayName("Caller")
                .maxPlayers(1).currentPlayers(0).build();
        Player player = Player.builder().id(10L).albionName("Jorge").build();

        when(avalonRunRepository.findById(1L)).thenReturn(Optional.of(openAvalon));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(registrationRepository.findActiveByAvalonIdAndPlayerId(1L, 10L)).thenReturn(Optional.empty());
        when(slotRepository.findByAvalonIdAndSlotKeyForUpdate(1L, "CALLER")).thenReturn(Optional.of(slot));
        when(registrationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(slotRepository.findByAvalonId(1L)).thenReturn(List.of(slot));
        when(registrationRepository.findActiveByAvalonId(1L)).thenReturn(List.of());
        when(roleBuildTemplateService.findAllAsMap()).thenReturn(Map.of());

        AvalonRolesOverviewResponse response = avalonRoleService.joinRole(1L, RoleType.CALLER);

        assertNotNull(response);
        verify(slotRepository).save(argThat(s -> s.getCurrentPlayers() == 1));
    }

    @Test
    @DisplayName("Join role throws ROLE_FULL when slot is full")
    void joinRole_roleFull() {
        setCurrentPlayer(10L);
        AvalonRoleSlot slot = AvalonRoleSlot.builder()
                .id(1L).avalonRun(openAvalon).roleType(RoleType.CALLER)
                .slotKey("CALLER").displayName("Caller")
                .maxPlayers(1).currentPlayers(1).build();
        Player player = Player.builder().id(10L).albionName("Jorge").build();

        when(avalonRunRepository.findById(1L)).thenReturn(Optional.of(openAvalon));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(registrationRepository.findActiveByAvalonIdAndPlayerId(1L, 10L)).thenReturn(Optional.empty());
        when(slotRepository.findByAvalonIdAndSlotKeyForUpdate(1L, "CALLER")).thenReturn(Optional.of(slot));

        assertThrows(RoleFullException.class, () -> avalonRoleService.joinRole(1L, RoleType.CALLER));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Leave role decrements current players")
    void leaveRole_success() {
        setCurrentPlayer(10L);
        AvalonRoleSlot slot = AvalonRoleSlot.builder()
                .id(1L).avalonRun(openAvalon).roleType(RoleType.DPS)
                .slotKey("DPS").displayName("DPS")
                .maxPlayers(5).currentPlayers(3).build();
        Player player = Player.builder().id(10L).albionName("Pedro").build();
        AvalonRoleRegistration registration = AvalonRoleRegistration.builder()
                .id(99L).avalonRun(openAvalon).player(player)
                .roleType(RoleType.DPS).slotKey("DPS").status(RegistrationStatus.ACTIVE).build();

        when(avalonRunRepository.findById(1L)).thenReturn(Optional.of(openAvalon));
        when(registrationRepository.findActiveByAvalonIdAndPlayerIdAndSlotKey(1L, 10L, "DPS"))
                .thenReturn(Optional.of(registration));
        when(slotRepository.findByAvalonIdAndSlotKeyForUpdate(1L, "DPS")).thenReturn(Optional.of(slot));
        when(slotRepository.findByAvalonId(1L)).thenReturn(List.of(slot));
        when(registrationRepository.findActiveByAvalonId(1L)).thenReturn(List.of());
        when(roleBuildTemplateService.findAllAsMap()).thenReturn(Map.of());

        avalonRoleService.leaveRole(1L, RoleType.DPS);

        verify(registrationRepository).save(argThat(r -> r.getStatus() == RegistrationStatus.CANCELLED));
        verify(slotRepository).save(argThat(s -> s.getCurrentPlayers() == 2));
    }

    @Test
    @DisplayName("Configure roles creates default slots")
    void configureRoles_success() {
        when(avalonRunRepository.findById(1L)).thenReturn(Optional.of(openAvalon));
        when(slotRepository.findByAvalonId(1L)).thenReturn(List.of());
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConfigureAvalonRolesRequest request = ConfigureAvalonRolesRequest.builder()
                .slots(List.of(
                        AvalonRoleSlotRequest.builder().roleType(RoleType.CALLER).maxPlayers(1).build(),
                        AvalonRoleSlotRequest.builder().roleType(RoleType.DPS).maxPlayers(5).build()))
                .build();

        avalonRoleService.configureRoles(1L, request);

        verify(slotRepository, times(2)).save(any());
    }

    private void setCurrentPlayer(Long playerId) {
        PlayerPrincipal principal = new PlayerPrincipal(playerId, "test", PlayerRole.PLAYER);
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())));
    }
}
