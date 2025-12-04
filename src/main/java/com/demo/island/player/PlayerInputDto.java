package com.demo.island.player;

import com.demo.island.dto.CheckResultDto;
import com.demo.island.dto.CreatureSnapshot;
import com.demo.island.dto.ItemSnapshot;
import com.demo.island.dto.PlayerSnapshot;
import com.demo.island.dto.SessionSnapshot;
import com.demo.island.dto.TileSnapshot;

import java.util.List;

public final class PlayerInputDto {

    private final SessionSnapshot session;
    private final PlayerSnapshot player;
    private final TileSnapshot currentTile;
    private final List<ItemSnapshot> visibleItems;
    private final List<CreatureSnapshot> nearbyCreatures;
    private final List<CheckResultDto> recentCheckResults;
    private final String recentNarration;

    public PlayerInputDto(SessionSnapshot session, PlayerSnapshot player, TileSnapshot currentTile,
                          List<ItemSnapshot> visibleItems, List<CreatureSnapshot> nearbyCreatures,
                          List<CheckResultDto> recentCheckResults, String recentNarration) {
        this.session = session;
        this.player = player;
        this.currentTile = currentTile;
        this.visibleItems = visibleItems == null ? List.of() : List.copyOf(visibleItems);
        this.nearbyCreatures = nearbyCreatures == null ? List.of() : List.copyOf(nearbyCreatures);
        this.recentCheckResults = recentCheckResults == null ? List.of() : List.copyOf(recentCheckResults);
        this.recentNarration = recentNarration;
    }

    public SessionSnapshot getSession() {
        return session;
    }

    public PlayerSnapshot getPlayer() {
        return player;
    }

    public TileSnapshot getCurrentTile() {
        return currentTile;
    }

    public List<ItemSnapshot> getVisibleItems() {
        return visibleItems;
    }

    public List<CreatureSnapshot> getNearbyCreatures() {
        return nearbyCreatures;
    }

    public List<CheckResultDto> getRecentCheckResults() {
        return recentCheckResults;
    }

    public String getRecentNarration() {
        return recentNarration;
    }
}
