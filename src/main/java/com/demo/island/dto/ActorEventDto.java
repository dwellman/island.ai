package com.demo.island.dto;

public final class ActorEventDto {

    private final int turnNumber;
    private final String locationTileId;
    private final String eventType;
    private final String otherActorId;
    private final String summary;

    public ActorEventDto(int turnNumber, String locationTileId, String eventType, String otherActorId, String summary) {
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
