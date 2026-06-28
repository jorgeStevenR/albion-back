package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncGuildResponse {

    private String guild;
    private int playersImported;
    private int updated;
    private int created;
    private boolean skipped;
    private LocalDateTime lastSyncAt;
}
