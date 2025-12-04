package com.demo.island.sim;

import com.demo.island.world.GameOverReason;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TurnClock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Result of a Gardener random walk over plots.
 */
public final class GardenerRunResult {

    private final List<GardenerStepLog> steps;
    private final int stepsTaken;
    private final int finalTurnIndex;
    private final TurnClock.TimePhase finalPhase;
    private final String finalPlotId;
    private final boolean gameOver;
    private final GameOverReason gameOverReason;
    private final Map<TerrainDifficulty, Integer> difficultyEntries;
    private final int blockedNoTileCount;
    private final int blockedBoundaryCount;
    private final int blockedImpossibleCount;
    private final int fatalDeadCount;
    private final int uniquePlotsVisited;
    private final int uniqueAnchorPlotsVisited;

    public GardenerRunResult(
            List<GardenerStepLog> steps,
            int finalTurnIndex,
            TurnClock.TimePhase finalPhase,
            String finalPlotId,
            boolean gameOver,
            GameOverReason gameOverReason,
            Map<TerrainDifficulty, Integer> difficultyEntries,
            int blockedNoTileCount,
            int blockedBoundaryCount,
            int blockedImpossibleCount,
            int fatalDeadCount,
            int uniquePlotsVisited,
            int uniqueAnchorPlotsVisited
    ) {
        this.steps = List.copyOf(steps);
        this.stepsTaken = steps.size();
        this.finalTurnIndex = finalTurnIndex;
        this.finalPhase = finalPhase;
        this.finalPlotId = finalPlotId;
        this.gameOver = gameOver;
        this.gameOverReason = gameOverReason;
        Map<TerrainDifficulty, Integer> copy = new EnumMap<>(TerrainDifficulty.class);
        for (TerrainDifficulty d : TerrainDifficulty.values()) {
            copy.put(d, difficultyEntries.getOrDefault(d, 0));
        }
        this.difficultyEntries = Collections.unmodifiableMap(copy);
        this.blockedNoTileCount = blockedNoTileCount;
        this.blockedBoundaryCount = blockedBoundaryCount;
        this.blockedImpossibleCount = blockedImpossibleCount;
        this.fatalDeadCount = fatalDeadCount;
        this.uniquePlotsVisited = uniquePlotsVisited;
        this.uniqueAnchorPlotsVisited = uniqueAnchorPlotsVisited;
    }

    public List<GardenerStepLog> getSteps() {
        return steps;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public int getFinalTurnIndex() {
        return finalTurnIndex;
    }

    public TurnClock.TimePhase getFinalPhase() {
        return finalPhase;
    }

    public String getFinalPlotId() {
        return finalPlotId;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public GameOverReason getGameOverReason() {
        return gameOverReason;
    }

    public Map<TerrainDifficulty, Integer> getDifficultyEntries() {
        return difficultyEntries;
    }

    public int getBlockedNoTileCount() {
        return blockedNoTileCount;
    }

    public int getBlockedBoundaryCount() {
        return blockedBoundaryCount;
    }

    public int getBlockedImpossibleCount() {
        return blockedImpossibleCount;
    }

    public int getFatalDeadCount() {
        return fatalDeadCount;
    }

    public int getUniquePlotsVisited() {
        return uniquePlotsVisited;
    }

    public int getUniqueAnchorPlotsVisited() {
        return uniqueAnchorPlotsVisited;
    }
}
