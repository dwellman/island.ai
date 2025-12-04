package com.demo.island.game;

/**
 * Game-layer clock measured in pips (minutes).
 */
public final class CosmosClock {

    public enum Phase {
        PRE_DAWN,
        MORNING,
        AFTERNOON,
        DUSK,
        NIGHT
    }

    public static final int DEFAULT_MAX_PIPS = 1440; // 24 hours

    private int totalPips;
    private final int maxPips;
    private Phase phase;
    private boolean outOfTime;

    public CosmosClock() {
        this(DEFAULT_MAX_PIPS);
    }

    public CosmosClock(int maxPips) {
        this.totalPips = 0;
        this.maxPips = maxPips;
        recompute();
    }

    public int getTotalPips() {
        return totalPips;
    }

    public int getMaxPips() {
        return maxPips;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isOutOfTime() {
        return outOfTime;
    }

    public String formatRemainingBracketed() {
        int remaining = maxPips - totalPips;
        if (remaining < 0) remaining = 0;
        int hours = remaining / 60;
        int minutes = remaining % 60;
        return String.format("[%02d:%02d]", hours, minutes);
    }

    public void advance(int pips) {
        if (pips <= 0) {
            return;
        }
        totalPips += pips;
        recompute();
    }

    private void recompute() {
        outOfTime = totalPips >= maxPips;
        if (totalPips < 60) {
            phase = Phase.PRE_DAWN;
        } else if (totalPips < 360) {
            phase = Phase.MORNING;
        } else if (totalPips < 840) {
            phase = Phase.AFTERNOON;
        } else if (totalPips < 960) {
            phase = Phase.DUSK;
        } else {
            phase = Phase.NIGHT;
        }
    }

    public boolean isFirstSunriseMoment() {
        return totalPips == 60;
    }

    public boolean isBuzzerSunriseMoment() {
        return totalPips >= maxPips;
    }
}
