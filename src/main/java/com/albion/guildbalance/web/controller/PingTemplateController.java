package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.CreateAvalonFromTemplateRequest;
import com.albion.guildbalance.application.dto.request.SavePingTemplateRequest;
import com.albion.guildbalance.application.dto.response.PingTemplateResponse;
import com.albion.guildbalance.application.service.PingTemplateService;
import com.albion.guildbalance.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ping-templates")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ping Templates", description = "Reusable avalon ping templates for callers")
public class PingTemplateController {

    private final PingTemplateService pingTemplateService;

    @GetMapping
    @Operation(summary = "List active ping templates")
    public ResponseEntity<ApiResponse<List<PingTemplateResponse>>> findActive() {
        return ResponseEntity.ok(ApiResponse.success(pingTemplateService.findAllActive()));
    }

    @GetMapping("/all")
    @Operation(summary = "List all ping templates including inactive (admin)")
    public ResponseEntity<ApiResponse<List<PingTemplateResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(pingTemplateService.findAll()));
    }

    @PostMapping
    @Operation(summary = "Create a ping template (admin)")
    public ResponseEntity<ApiResponse<PingTemplateResponse>> create(@Valid @RequestBody SavePingTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plantilla creada", pingTemplateService.create(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a ping template (admin)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        pingTemplateService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Plantilla desactivada", null));
    }

    @PostMapping("/{id}/create-avalon")
    @Operation(summary = "Create avalon run from template")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createAvalon(
            @PathVariable Long id,
            @RequestBody(required = false) CreateAvalonFromTemplateRequest request) {
        Long avalonId = pingTemplateService.createAvalonFromTemplate(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Avaloniana creada", Map.of("avalonId", avalonId)));
    }
}
