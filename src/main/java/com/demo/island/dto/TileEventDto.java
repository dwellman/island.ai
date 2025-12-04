package com.demo.island.dto;

public final class TileEventDto {

    private final String tileId;
    private final int turnNumber;
    private final String actorId;
    private final String eventType;
    private final String summary;

    public TileEventDto(String tileId, int turnNumber, String actorId, String eventType, String summary) {
        this.tileId = tileId;
        this.turnNumber = turnNumber;
        this.actorId = actorId;
        this.eventType = eventType;
        this.summary = summary;
    }

    public String getTileId() {
        return tileId;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getActorId() {
        return actorId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSummary() {
        return summary;
    }
}
