package com.demo.island.world;

/**
 * Configuration for the deterministic Gardener sanity pass.
 */
public final class GardenerWorldConfig {

    private final int maxSteps;
    private final long randomSeed;

    public GardenerWorldConfig(int maxSteps, long randomSeed) {
        this.maxSteps = maxSteps;
        this.randomSeed = randomSeed;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public static GardenerWorldConfig defaultConfig() {
        return new GardenerWorldConfig(500, 42L);
    }
}
