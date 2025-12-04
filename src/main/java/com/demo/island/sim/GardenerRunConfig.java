package com.demo.island.sim;

/**
 * Configuration for a Gardener random walk.
 */
public final class GardenerRunConfig {
    private final int maxSteps;
    private final long randomSeed;

    public GardenerRunConfig(int maxSteps, long randomSeed) {
        this.maxSteps = maxSteps;
        this.randomSeed = randomSeed;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public long getRandomSeed() {
        return randomSeed;
    }
}
