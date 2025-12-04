package com.demo.island.sim;

public final class ToolEpisodeConfig {
    private final int maxTurns;

    public ToolEpisodeConfig() {
        this(10);
    }

    public ToolEpisodeConfig(int maxTurns) {
        this.maxTurns = maxTurns;
    }

    public int getMaxTurns() {
        return maxTurns;
    }
}
