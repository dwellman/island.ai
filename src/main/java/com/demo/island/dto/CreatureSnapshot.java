package com.demo.island.dto;

import java.util.List;
import java.util.Map;

public final class CreatureSnapshot {

    private final String creatureId;
    private final String kind;
    private final String currentTileId;
    private final Map<String, Integer> stats;
    private final List<String> carriedItemIds;
    private final String targetTileId;
    private final List<ActorEventDto> recentEvents;

    public CreatureSnapshot(String creatureId, String kind, String currentTileId, Map<String, Integer> stats,
                            List<String> carriedItemIds, String targetTileId, List<ActorEventDto> recentEvents) {
        this.creatureId = creatureId;
        this.kind = kind;
        this.currentTileId = currentTileId;
        this.stats = stats;
        this.carriedItemIds = carriedItemIds;
        this.targetTileId = targetTileId;
        this.recentEvents = recentEvents;
    }

    public String getCreatureId() {
        return creatureId;
    }

    public String getKind() {
        return kind;
    }

    public String getCurrentTileId() {
        return currentTileId;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public List<String> getCarriedItemIds() {
        return carriedItemIds;
    }

    public String getTargetTileId() {
        return targetTileId;
    }

    public List<ActorEventDto> getRecentEvents() {
        return recentEvents;
    }
}
