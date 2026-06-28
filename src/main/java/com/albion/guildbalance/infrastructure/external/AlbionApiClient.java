package com.albion.guildbalance.infrastructure.external;

import com.albion.guildbalance.application.dto.albion.AlbionGuildResponse;
import com.albion.guildbalance.application.dto.albion.AlbionPlayerResponse;
import com.albion.guildbalance.application.dto.albion.AlbionSearchResponse;
import com.albion.guildbalance.application.exception.AlbionApiException;
import com.albion.guildbalance.application.exception.GuildNotFoundException;
import com.albion.guildbalance.application.port.AlbionApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class AlbionApiClient implements AlbionApiPort {

    private final RestClient restClient;

    public AlbionApiClient(@Value("${albion.api.base-url:https://gameinfo.albiononline.com/api/gameinfo}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public AlbionGuildResponse searchGuild(String guildName) {
        String normalizedQuery = normalizeGuildName(guildName);
        log.info("Searching Albion guild: {} (normalized: {})", guildName, normalizedQuery);
        try {
            AlbionSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search").queryParam("q", normalizedQuery).build())
                    .retrieve()
                    .body(AlbionSearchResponse.class);

            if (response == null || response.getGuilds() == null || response.getGuilds().isEmpty()) {
                throw new GuildNotFoundException("Guild not found: " + guildName);
            }

            return response.getGuilds().stream()
                    .filter(g -> g.getName() != null)
                    .filter(g -> normalizeGuildName(g.getName()).equalsIgnoreCase(normalizedQuery))
                    .findFirst()
                    .orElseGet(() -> response.getGuilds().stream()
                            .min(Comparator.comparingInt(g -> levenshtein(normalizedQuery, normalizeGuildName(g.getName()))))
                            .orElseThrow(() -> new GuildNotFoundException("Guild not found: " + guildName)));
        } catch (GuildNotFoundException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AlbionApiException("Failed to search guild in Albion API", ex);
        }
    }

    static String normalizeGuildName(String name) {
        if (name == null) {
            return "";
        }
        return name.replace('|', ' ').trim().replaceAll("\\s+", " ");
    }

    @Override
    public List<AlbionPlayerResponse> getGuildMembers(String guildId) {
        log.info("Fetching Albion guild members for guildId: {}", guildId);
        try {
            List<AlbionPlayerResponse> members = restClient.get()
                    .uri("/guilds/{guildId}/members", guildId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (members == null) {
                throw new AlbionApiException("Empty response from Albion members API");
            }
            return members;
        } catch (AlbionApiException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AlbionApiException("Failed to fetch guild members from Albion API", ex);
        }
    }

    private static int levenshtein(String a, String b) {
        if (a == null || b == null) return Integer.MAX_VALUE;
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = Character.toLowerCase(a.charAt(i - 1)) == Character.toLowerCase(b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
