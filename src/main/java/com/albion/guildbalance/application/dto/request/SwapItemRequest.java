package com.albion.guildbalance.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapItemRequest {

    @NotBlank
    private String itemUniqueName;

    @NotBlank
    private String itemDisplayName;

    private String note;

    private Integer sortOrder;
}
