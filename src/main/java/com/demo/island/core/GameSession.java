package com.demo.island.core;

public final class GameSession {

    public enum TimePhase {
        LIGHT,
        DUSK,
        DARK
    }

    private final String sessionId;
    private int turnNumber;
    private final int maxTurns;
    private TimePhase timePhase;
    private boolean midnightReached;
    private boolean ghostAwakened;

    public GameSession(String sessionId, int maxTurns) {
        this.sessionId = sessionId;
        this.maxTurns = maxTurns;
        this.turnNumber = 0;
        this.timePhase = TimePhase.LIGHT;
        this.midnightReached = false;
        this.ghostAwakened = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public void advanceTurn() {
        this.turnNumber += 1;
        if (turnNumber >= maxTurns) {
            midnightReached = true;
        }
    }

    public TimePhase getTimePhase() {
        return timePhase;
    }

    public void setTimePhase(TimePhase timePhase) {
        this.timePhase = timePhase;
    }

    public boolean isMidnightReached() {
        return midnightReached;
    }

    public void setMidnightReached(boolean midnightReached) {
        this.midnightReached = midnightReached;
    }

    public boolean isGhostAwakened() {
        return ghostAwakened;
    }

    public void setGhostAwakened(boolean ghostAwakened) {
        this.ghostAwakened = ghostAwakened;
    }
}
