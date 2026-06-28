package com.albion.guildbalance.application.port;

import com.albion.guildbalance.application.dto.albion.AlbionGuildResponse;
import com.albion.guildbalance.application.dto.albion.AlbionPlayerResponse;

import java.util.List;

public interface AlbionApiPort {

    AlbionGuildResponse searchGuild(String guildName);

    List<AlbionPlayerResponse> getGuildMembers(String guildId);
}
