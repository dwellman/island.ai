package com.demo.island.game;

import com.demo.island.world.TerrainDifficulty;

/**
 * Provides time costs (in pips) for game actions.
 */
public final class GameActionCost {

    private GameActionCost() {
    }

    public static int timeCost(GameActionType type, TerrainDifficulty difficulty) {
        return switch (type) {
            case MOVE_WALK -> walkCost(difficulty);
            case MOVE_RUN -> runCost(difficulty);
            case JUMP -> walkCost(difficulty);
            case LOOK -> 1;
            case SEARCH -> 3;
            case PICK_UP -> 1;
            case DROP -> 1;
            case RAFT_WORK_SMALL -> 5;
            case RAFT_WORK_MAJOR -> 15;
            case LAUNCH_RAFT -> 5;
        };
    }

    private static int walkCost(TerrainDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 4;
            case NORMAL -> 5;
            case HARD -> 7;
            case EXTREME -> 10;
        };
    }

    private static int runCost(TerrainDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 2;
            case NORMAL -> 3;
            case HARD -> 4;
            case EXTREME -> 6; // allow but costly
        };
    }
}
