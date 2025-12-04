package com.demo.island.monkey;

import com.demo.island.dto.CheckResultDto;
import com.demo.island.dto.CreatureSnapshot;
import com.demo.island.dto.PlayerSnapshot;
import com.demo.island.dto.SessionSnapshot;

import java.util.List;

public final class MonkeyInputDto {

    private final SessionSnapshot session;
    private final CreatureSnapshot monkey;
    private final String homeTileId;
    private final List<PlayerSnapshot> players;
    private final List<CheckResultDto> recentCheckResults;

    public MonkeyInputDto(SessionSnapshot session, CreatureSnapshot monkey, String homeTileId,
                          List<PlayerSnapshot> players, List<CheckResultDto> recentCheckResults) {
        this.session = session;
        this.monkey = monkey;
        this.homeTileId = homeTileId;
        this.players = players;
        this.recentCheckResults = recentCheckResults;
    }

    public SessionSnapshot getSession() {
        return session;
    }

    public CreatureSnapshot getMonkey() {
        return monkey;
    }

    public String getHomeTileId() {
        return homeTileId;
    }

    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    public List<CheckResultDto> getRecentCheckResults() {
        return recentCheckResults;
    }
}
