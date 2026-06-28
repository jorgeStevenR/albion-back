package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminManualPenaltyRequest extends ManualPenaltyRequest {

    @NotNull
    private Long avalonId;
}
