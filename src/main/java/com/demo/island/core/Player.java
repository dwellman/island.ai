package com.demo.island.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Player {

    private final String playerId;
    private final String name;
    private final String avatarType;
    private final Stats stats = new Stats();
    private String currentTileId;
    private final List<String> inventoryItemIds = new ArrayList<>();
    private TextFace textFace;
    private static final int MAX_RECENT_EVENTS = 20;
    private final LinkedList<ActorEvent> recentEvents = new LinkedList<>();

    public Player(String playerId, String name, String avatarType, String startingTileId, TextFace textFace) {
        this.playerId = Objects.requireNonNull(playerId);
        this.name = Objects.requireNonNull(name);
        this.avatarType = Objects.requireNonNull(avatarType);
        this.currentTileId = Objects.requireNonNull(startingTileId);
        this.textFace = Objects.requireNonNull(textFace);
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

    public Stats getStats() {
        return stats;
    }

    public String getCurrentTileId() {
        return currentTileId;
    }

    public void moveToTile(String tileId) {
        this.currentTileId = Objects.requireNonNull(tileId);
    }

    public List<String> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public void addItem(String itemId) {
        inventoryItemIds.add(Objects.requireNonNull(itemId));
    }

    public void removeItem(String itemId) {
        inventoryItemIds.remove(itemId);
    }

    public TextFace getTextFace() {
        return textFace;
    }

    public void setTextFace(TextFace textFace) {
        this.textFace = Objects.requireNonNull(textFace);
    }

    public void recordEvent(ActorEvent event) {
        if (event == null) {
            return;
        }
        recentEvents.addFirst(event);
        while (recentEvents.size() > MAX_RECENT_EVENTS) {
            recentEvents.removeLast();
        }
    }

    public List<ActorEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }
}
