package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.MoneyRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewMoneyRequestDto {

    @NotNull
    private MoneyRequestStatus status;

    private String reviewNotes;
}
