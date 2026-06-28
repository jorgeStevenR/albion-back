package com.albion.guildbalance.application.dto.albion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbionSearchResponse {

    @JsonProperty("guilds")
    @Builder.Default
    private List<AlbionGuildResponse> guilds = new ArrayList<>();

    @JsonProperty("players")
    @Builder.Default
    private List<AlbionPlayerResponse> players = new ArrayList<>();
}
