package com.demo.island.world;

public final class TileHistoryEntry {

    private final int sequenceNumber;
    private final int turnIndex;
    private final TurnClock.TimePhase phase;
    private final String eventType;
    private final String summary;

    public TileHistoryEntry(int sequenceNumber, int turnIndex, TurnClock.TimePhase phase, String eventType, String summary) {
        this.sequenceNumber = sequenceNumber;
        this.turnIndex = turnIndex;
        this.phase = phase;
        this.eventType = eventType;
        this.summary = summary;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public TurnClock.TimePhase getPhase() {
        return phase;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSummary() {
        return summary;
    }
}
