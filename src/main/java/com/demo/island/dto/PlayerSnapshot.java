package com.demo.island.dto;

import java.util.List;
import java.util.Map;

public final class PlayerSnapshot {

    private final String playerId;
    private final String name;
    private final String avatarType;
    private final String currentTileId;
    private final Map<String, Integer> stats;
    private final List<String> inventoryItemIds;
    private final List<ActorEventDto> recentEvents;

    public PlayerSnapshot(String playerId, String name, String avatarType, String currentTileId,
                          Map<String, Integer> stats, List<String> inventoryItemIds, List<ActorEventDto> recentEvents) {
        this.playerId = playerId;
        this.name = name;
        this.avatarType = avatarType;
        this.currentTileId = currentTileId;
        this.stats = stats;
        this.inventoryItemIds = inventoryItemIds;
        this.recentEvents = recentEvents;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public String getAvatarType() {
        return avatarType;
    }

    public String getCurrentTileId() {
        return currentTileId;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public List<String> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public List<ActorEventDto> getRecentEvents() {
        return recentEvents;
    }
}
