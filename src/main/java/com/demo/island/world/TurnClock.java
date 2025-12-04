package com.demo.island.world;

/**
 * Discrete turn-based clock with fixed phase thresholds and turn limit.
 */
public final class TurnClock {

    public enum TimePhase {
        LIGHT,
        DUSK,
        DARK
    }

    public static final int TURN_LIMIT = 36;

    private int turnIndex;
    private TimePhase phase;
    private boolean outOfTime;

    public TurnClock() {
        this.turnIndex = 0;
        recompute();
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public TimePhase getPhase() {
        return phase;
    }

    public boolean isOutOfTime() {
        return outOfTime;
    }

    /**
        * Apply a movement outcome; only successful moves consume time.
        */
    public void applyMoveOutcome(MoveOutcome outcome) {
        applyMoveOutcome(outcome, 1);
    }

    public void applyMoveOutcome(MoveOutcome outcome, int timeCost) {
        if (outcome == MoveOutcome.MOVE_OK) {
            turnIndex += timeCost;
            recompute();
        }
    }

    public void applyTimeCost(int timeCost) {
        turnIndex += timeCost;
        recompute();
    }

    private void recompute() {
        this.phase = phaseFor(turnIndex);
        this.outOfTime = turnIndex >= TURN_LIMIT;
    }

    public static TimePhase phaseFor(int index) {
        if (index <= 11) {
            return TimePhase.LIGHT;
        }
        if (index <= 23) {
            return TimePhase.DUSK;
        }
        return TimePhase.DARK;
    }
}
