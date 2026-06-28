package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvalonRunRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalDateTime scheduledAt;

    @NotBlank(message = "Zone is required")
    private String zone;

    private String description;
}
