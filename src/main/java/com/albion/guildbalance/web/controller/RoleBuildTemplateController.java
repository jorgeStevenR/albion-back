package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.SaveRoleBuildTemplateRequest;
import com.albion.guildbalance.application.dto.response.RoleBuildTemplateResponse;
import com.albion.guildbalance.application.service.RoleBuildTemplateService;
import com.albion.guildbalance.domain.enums.RoleType;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/build-templates")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Role Build Templates", description = "Recommended builds per Avalon role")
public class RoleBuildTemplateController {

    private final RoleBuildTemplateService roleBuildTemplateService;

    @GetMapping
    @Operation(summary = "List all role build templates")
    public ResponseEntity<ApiResponse<List<RoleBuildTemplateResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(roleBuildTemplateService.findAll()));
    }

    @GetMapping("/{roleType}")
    @Operation(summary = "Get build template for a role")
    public ResponseEntity<ApiResponse<RoleBuildTemplateResponse>> findByRole(@PathVariable RoleType roleType) {
        return ResponseEntity.ok(ApiResponse.success(roleBuildTemplateService.findByRoleType(roleType)));
    }

    @PutMapping("/{roleType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CALLER', 'OFFICER')")
    @Operation(summary = "Save build template for a role")
    public ResponseEntity<ApiResponse<RoleBuildTemplateResponse>> save(
            @PathVariable RoleType roleType,
            @Valid @RequestBody SaveRoleBuildTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Build template saved", roleBuildTemplateService.save(roleType, request)));
    }
}
