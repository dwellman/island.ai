package com.demo.island.dto;

public final class SessionSnapshot {

    private final String sessionId;
    private final int turnNumber;
    private final int maxTurns;
    private final String timePhase;
    private final boolean midnightReached;
    private final boolean ghostAwakened;
    private final int raftProgress;

    public SessionSnapshot(String sessionId, int turnNumber, int maxTurns, String timePhase,
                           boolean midnightReached, boolean ghostAwakened, int raftProgress) {
        this.sessionId = sessionId;
        this.turnNumber = turnNumber;
        this.maxTurns = maxTurns;
        this.timePhase = timePhase;
        this.midnightReached = midnightReached;
        this.ghostAwakened = ghostAwakened;
        this.raftProgress = raftProgress;
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

    public String getTimePhase() {
        return timePhase;
    }

    public boolean isMidnightReached() {
        return midnightReached;
    }

    public boolean isGhostAwakened() {
        return ghostAwakened;
    }

    public int getRaftProgress() {
        return raftProgress;
    }
}
