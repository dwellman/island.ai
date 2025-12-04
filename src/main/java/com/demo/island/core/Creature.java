package com.demo.island.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Creature {

    public enum CreatureKind {
        GHOST,
        MONKEY_TROOP
    }

    private final String creatureId;
    private final CreatureKind kind;
    private String currentTileId;
    private final Stats stats = new Stats();
    private final List<String> carriedItemIds = new ArrayList<>();
    private TextFace textFace;
    private String targetTileId;
    private static final int MAX_RECENT_EVENTS = 20;
    private final LinkedList<ActorEvent> recentEvents = new LinkedList<>();

    public Creature(String creatureId, CreatureKind kind, String startingTileId, TextFace textFace) {
        this.creatureId = Objects.requireNonNull(creatureId);
        this.kind = Objects.requireNonNull(kind);
        this.currentTileId = Objects.requireNonNull(startingTileId);
        this.textFace = Objects.requireNonNull(textFace);
        this.targetTileId = null;
    }

    public String getCreatureId() {
        return creatureId;
    }

    public CreatureKind getKind() {
        return kind;
    }

    public String getCurrentTileId() {
        return currentTileId;
    }

    public void moveToTile(String tileId) {
        this.currentTileId = Objects.requireNonNull(tileId);
    }

    public Stats getStats() {
        return stats;
    }

    public List<String> getCarriedItemIds() {
        return carriedItemIds;
    }

    public TextFace getTextFace() {
        return textFace;
    }

    public void setTextFace(TextFace textFace) {
        this.textFace = Objects.requireNonNull(textFace);
    }

    public String getTargetTileId() {
        return targetTileId;
    }

    public void setTargetTileId(String targetTileId) {
        this.targetTileId = targetTileId;
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
