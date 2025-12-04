package com.demo.island.world;

public final class MoveResult {

    private final MoveOutcome outcome;
    private final PlayerLocation location;
    private final int timeCost;
    private final boolean gameOver;
    private final GameOverReason gameOverReason;

    public MoveResult(MoveOutcome outcome, PlayerLocation location, int timeCost, boolean gameOver, GameOverReason gameOverReason) {
        this.outcome = outcome;
        this.location = location;
        this.timeCost = timeCost;
        this.gameOver = gameOver;
        this.gameOverReason = gameOverReason;
    }

    public MoveOutcome getOutcome() {
        return outcome;
    }

    public PlayerLocation getLocation() {
        return location;
    }

    public int getTimeCost() {
        return timeCost;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public GameOverReason getGameOverReason() {
        return gameOverReason;
    }
}
