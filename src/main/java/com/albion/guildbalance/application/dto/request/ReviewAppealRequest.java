package com.albion.guildbalance.application.dto.request;

import com.albion.guildbalance.domain.enums.AppealStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewAppealRequest {

    @NotNull
    private AppealStatus decision;

    @Size(max = 1000)
    private String reviewNotes;
}
