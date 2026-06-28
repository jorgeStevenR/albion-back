package com.albion.guildbalance.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildPlayerResponse {

    private Long id;
    private String albionName;
    private String rank;
    private boolean active;
    private java.math.BigDecimal balance;
    private java.math.BigDecimal totalEarned;
    private long avalonCount;
}
