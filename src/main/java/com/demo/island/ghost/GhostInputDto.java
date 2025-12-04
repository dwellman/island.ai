package com.demo.island.ghost;

import com.demo.island.dto.CheckResultDto;
import com.demo.island.dto.CreatureSnapshot;
import com.demo.island.dto.PlayerSnapshot;
import com.demo.island.dto.SessionSnapshot;

import java.util.List;

public final class GhostInputDto {

    private final SessionSnapshot session;
    private final List<CreatureSnapshot> ghosts;
    private final List<PlayerSnapshot> players;
    private final List<CheckResultDto> recentCheckResults;

    public GhostInputDto(SessionSnapshot session, List<CreatureSnapshot> ghosts, List<PlayerSnapshot> players,
                         List<CheckResultDto> recentCheckResults) {
        this.session = session;
        this.ghosts = ghosts;
        this.players = players;
        this.recentCheckResults = recentCheckResults;
    }

    public SessionSnapshot getSession() {
        return session;
    }

    public List<CreatureSnapshot> getGhosts() {
        return ghosts;
    }

    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    public List<CheckResultDto> getRecentCheckResults() {
        return recentCheckResults;
    }
}
