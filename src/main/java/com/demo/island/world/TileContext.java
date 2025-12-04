package com.demo.island.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Mutable context for a tile that AI can extend over time.
 */
public final class TileContext {

    private final String baseDescription;
    private String currentDescription;
    private final List<TileHistoryEntry> history = new ArrayList<>();
    private int sequenceCounter = 0;

    public TileContext(String baseDescription) {
        this.baseDescription = Objects.requireNonNull(baseDescription);
        this.currentDescription = baseDescription;
    }

    public String getBaseDescription() {
        return baseDescription;
    }

    public String getCurrentDescription() {
        return currentDescription;
    }

    public void setCurrentDescription(String currentDescription) {
        this.currentDescription = Objects.requireNonNull(currentDescription);
    }

    public List<TileHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void appendHistory(int turnIndex, TurnClock.TimePhase phase, String eventType, String summary) {
        history.add(new TileHistoryEntry(++sequenceCounter, turnIndex, phase, eventType, summary));
    }
}
