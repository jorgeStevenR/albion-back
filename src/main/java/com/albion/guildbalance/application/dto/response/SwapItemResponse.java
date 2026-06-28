package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapItemResponse {

    private String itemUniqueName;
    private String itemDisplayName;
    private String iconUrl;
    private String note;
    private int sortOrder;
}
