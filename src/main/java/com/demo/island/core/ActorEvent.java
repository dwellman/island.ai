package com.demo.island.core;

/**
 * Lightweight event record attached to an actor (player or creature).
 */
public final class ActorEvent {

    private final int turnNumber;
    private final String locationTileId;
    private final String eventType;
    private final String otherActorId;
    private final String summary;

    public ActorEvent(int turnNumber, String locationTileId, String eventType, String otherActorId, String summary) {
        this.turnNumber = turnNumber;
        this.locationTileId = locationTileId;
        this.eventType = eventType;
        this.otherActorId = otherActorId;
        this.summary = summary;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getLocationTileId() {
        return locationTileId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOtherActorId() {
        return otherActorId;
    }

    public String getSummary() {
        return summary;
    }
}
