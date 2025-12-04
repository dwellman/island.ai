package com.demo.island.world;

public enum TerrainDifficulty {
    EASY(1),
    NORMAL(1),
    HARD(2),
    EXTREME(3);

    private final int timeCost;

    TerrainDifficulty(int timeCost) {
        this.timeCost = timeCost;
    }

    public int getTimeCost() {
        return timeCost;
    }
}
