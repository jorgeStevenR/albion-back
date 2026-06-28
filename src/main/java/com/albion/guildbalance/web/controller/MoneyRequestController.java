package com.albion.guildbalance.web.controller;

import com.albion.guildbalance.application.dto.request.CreateMoneyRequestDto;
import com.albion.guildbalance.application.dto.request.ReviewMoneyRequestDto;
import com.albion.guildbalance.application.dto.response.MoneyRequestResponse;
import com.albion.guildbalance.application.service.MoneyRequestService;
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

@RestController
@RequestMapping("/api/money-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Money Requests", description = "Withdrawals and loans")
public class MoneyRequestController {

    private final MoneyRequestService moneyRequestService;

    @PostMapping("/withdrawals")
    @Operation(summary = "Create a withdrawal/advance request")
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> createWithdrawal(
            @Valid @RequestBody CreateMoneyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solicitud enviada", moneyRequestService.createWithdrawal(request)));
    }

    @PostMapping("/loans")
    @Operation(summary = "Create a loan request")
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> createLoan(
            @Valid @RequestBody CreateMoneyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solicitud de préstamo enviada", moneyRequestService.createLoan(request)));
    }

    @GetMapping("/withdrawals/mine")
    @Operation(summary = "List my withdrawal requests")
    public ResponseEntity<ApiResponse<List<MoneyRequestResponse>>> myWithdrawals() {
        return ResponseEntity.ok(ApiResponse.success(moneyRequestService.myWithdrawals()));
    }

    @GetMapping("/loans/mine")
    @Operation(summary = "List my loan requests")
    public ResponseEntity<ApiResponse<List<MoneyRequestResponse>>> myLoans() {
        return ResponseEntity.ok(ApiResponse.success(moneyRequestService.myLoans()));
    }

    @GetMapping("/withdrawals")
    @Operation(summary = "List all withdrawal requests (admin)")
    public ResponseEntity<ApiResponse<List<MoneyRequestResponse>>> allWithdrawals() {
        return ResponseEntity.ok(ApiResponse.success(moneyRequestService.allWithdrawals()));
    }

    @GetMapping("/loans")
    @Operation(summary = "List all loan requests (admin)")
    public ResponseEntity<ApiResponse<List<MoneyRequestResponse>>> allLoans() {
        return ResponseEntity.ok(ApiResponse.success(moneyRequestService.allLoans()));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Approve or reject a request (admin)")
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewMoneyRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Solicitud actualizada", moneyRequestService.review(id, request)));
    }
}
