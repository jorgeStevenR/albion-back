package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.ConfigureAvalonRolesRequest;
import com.albion.guildbalance.application.dto.request.UpdateRoleSlotRequest;
import com.albion.guildbalance.application.dto.response.AvalonRolesOverviewResponse;
import com.albion.guildbalance.application.service.AvalonRoleService;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avalons/{avalonId}/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Avalon Role Registration", description = "Party role signup for avalonian runs")
public class AvalonRoleController {

    private final AvalonRoleService avalonRoleService;

    @GetMapping
    @Operation(summary = "Get role slots and registrations for an avalon run")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> getRoles(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(avalonRoleService.getRoles(avalonId)));
    }

    @PostMapping("/{role}/join")
    @Operation(summary = "Register current player for a role (legacy)")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> joinRole(
            @PathVariable Long avalonId,
            @PathVariable RoleType role) {
        return ResponseEntity.ok(ApiResponse.success(
                "Registered for role " + role, avalonRoleService.joinRole(avalonId, role)));
    }

    @PostMapping("/slots/{slotKey}/join")
    @Operation(summary = "Register current player for a party slot")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> joinSlot(
            @PathVariable Long avalonId,
            @PathVariable String slotKey) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inscrito en " + slotKey, avalonRoleService.joinSlot(avalonId, slotKey)));
    }

    @DeleteMapping("/slots/{slotKey}/leave")
    @Operation(summary = "Cancel registration for a party slot")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> leaveSlot(
            @PathVariable Long avalonId,
            @PathVariable String slotKey) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inscripción cancelada", avalonRoleService.leaveSlot(avalonId, slotKey)));
    }

    @DeleteMapping("/{role}/leave")
    @Operation(summary = "Cancel current player's role registration")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> leaveRole(
            @PathVariable Long avalonId,
            @PathVariable RoleType role) {
        return ResponseEntity.ok(ApiResponse.success(
                "Registration cancelled", avalonRoleService.leaveRole(avalonId, role)));
    }

    @PostMapping("/setup")
    @Operation(summary = "Configure role slots (admin/officer)")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> configureRoles(
            @PathVariable Long avalonId,
            @Valid @RequestBody ConfigureAvalonRolesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Roles configured", avalonRoleService.configureRoles(avalonId, request)));
    }

    @PutMapping("/{role}")
    @Operation(summary = "Update max players for a role slot (admin/officer)")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> updateRoleSlot(
            @PathVariable Long avalonId,
            @PathVariable RoleType role,
            @Valid @RequestBody UpdateRoleSlotRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Role slot updated", avalonRoleService.updateRoleSlot(avalonId, role, request)));
    }

    @PostMapping("/close-registrations")
    @Operation(summary = "Close role registrations (admin/officer)")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> closeRegistrations(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Registrations closed", avalonRoleService.setRegistrationsOpen(avalonId, false)));
    }

    @PostMapping("/open-registrations")
    @Operation(summary = "Reopen role registrations (admin/officer)")
    public ResponseEntity<ApiResponse<AvalonRolesOverviewResponse>> openRegistrations(@PathVariable Long avalonId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Registrations opened", avalonRoleService.setRegistrationsOpen(avalonId, true)));
    }
}
