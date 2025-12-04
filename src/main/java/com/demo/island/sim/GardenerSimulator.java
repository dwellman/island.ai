package com.demo.island.sim;

import com.demo.island.world.Direction8;
import com.demo.island.world.GameOverReason;
import com.demo.island.world.IslandGardener;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandMovement;
import com.demo.island.world.IslandTile;
import com.demo.island.world.MoveOutcome;
import com.demo.island.world.MoveResult;
import com.demo.island.world.PlayerLocation;
import com.demo.island.world.Position;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TileKind;
import com.demo.island.world.TurnClock;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Runs a random-walk probe (Gardener actor) across plots to produce step-by-step logs.
 */
public final class GardenerSimulator {

    private GardenerSimulator() {
    }

    public static GardenerRunResult run(GardenerRunConfig config) {
        IslandMap map = IslandGardener.garden();
        return run(config, map);
    }

    public static GardenerRunResult run(GardenerRunConfig config, IslandMap map) {
        PlayerLocation location = PlayerLocation.spawn();
        TurnClock clock = new TurnClock();
        Random random = new Random(config.getRandomSeed());

        List<GardenerStepLog> steps = new ArrayList<>();
        Map<TerrainDifficulty, Integer> difficultyEntries = new EnumMap<>(TerrainDifficulty.class);
        for (TerrainDifficulty d : TerrainDifficulty.values()) {
            difficultyEntries.put(d, 0);
        }

        int blockedNoTile = 0;
        int blockedBoundary = 0;
        int blockedImpossible = 0;
        int fatalDead = 0;
        Set<String> uniquePlots = new HashSet<>();
        Set<String> uniqueAnchorPlots = new HashSet<>();

        Direction8[] directions = Direction8.values();
        boolean gameOver = false;
        GameOverReason gameOverReason = GameOverReason.NONE;

        for (int step = 1; step <= config.getMaxSteps(); step++) {
            if (gameOver) {
                break;
            }

            int turnBefore = clock.getTurnIndex();
            Direction8 direction = directions[random.nextInt(directions.length)];

            Position origin = location.getPosition(map);
            Position targetPos = origin.step(direction);
            IslandTile targetTile = map.get(targetPos).orElse(null);

            MoveResult moveResult = IslandMovement.move(map, location, direction, clock);
            location = moveResult.getLocation();
            String plotId = location.getTileId();
            IslandTile currentPlot = map.get(plotId)
                    .orElseThrow(() -> new IllegalStateException("Unknown plot id: " + plotId));

            GardenerStepLog log = new GardenerStepLog(
                    step,
                    direction,
                    turnBefore,
                    clock.getTurnIndex(),
                    clock.getPhase(),
                    currentPlot.getTileId(),
                    currentPlot.getPosition(),
                    currentPlot.getDifficulty(),
                    currentPlot.getSafety(),
                    currentPlot.getFeatures(),
                    currentPlot.getPrimaryPlantFamily(),
                    currentPlot.getSecondaryPlantFamilies(),
                    currentPlot.getPlantDensity(),
                    moveResult.getOutcome(),
                    moveResult.getTimeCost(),
                    moveResult.isGameOver(),
                    moveResult.getGameOverReason(),
                    currentPlot.getContext().getCurrentDescription(),
                    buildStepNote(moveResult, targetTile)
            );
            steps.add(log);

            if (moveResult.getOutcome() == MoveOutcome.MOVE_OK) {
                uniquePlots.add(currentPlot.getTileId());
                if (currentPlot.getKind() == TileKind.ANCHOR) {
                    uniqueAnchorPlots.add(currentPlot.getTileId());
                }
                difficultyEntries.put(currentPlot.getDifficulty(), difficultyEntries.get(currentPlot.getDifficulty()) + 1);
            } else {
                switch (moveResult.getOutcome()) {
                    case MOVE_BLOCKED_NO_TILE -> blockedNoTile++;
                    case MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY -> blockedBoundary++;
                    case MOVE_BLOCKED_IMPOSSIBLE -> blockedImpossible++;
                    case MOVE_FATAL_DEAD_TILE -> fatalDead++;
                    default -> {
                    }
                }
            }

            if (moveResult.isGameOver()) {
                gameOver = true;
                gameOverReason = moveResult.getGameOverReason();
            }
        }

        String finalPlotId = location.getTileId();
        return new GardenerRunResult(
                steps,
                clock.getTurnIndex(),
                clock.getPhase(),
                finalPlotId,
                gameOver,
                gameOverReason,
                difficultyEntries,
                blockedNoTile,
                blockedBoundary,
                blockedImpossible,
                fatalDead,
                uniquePlots.size(),
                uniqueAnchorPlots.size()
        );
    }

    private static String buildStepNote(MoveResult moveResult, IslandTile targetTile) {
        if (moveResult.getOutcome() == MoveOutcome.MOVE_OK) {
            return "Moved onto plot.";
        }
        if (moveResult.getOutcome() == MoveOutcome.MOVE_FATAL_DEAD_TILE) {
            return "Attempted to enter a DEAD plot.";
        }
        if (moveResult.getOutcome() == MoveOutcome.MOVE_BLOCKED_IMPOSSIBLE) {
            return "Blocked by IMPOSSIBLE terrain.";
        }
        if (moveResult.getOutcome() == MoveOutcome.MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY) {
            return "Blocked by island boundary.";
        }
        if (moveResult.getOutcome() == MoveOutcome.MOVE_BLOCKED_NO_TILE) {
            return targetTile == null ? "No plot in that direction." : "No traversable plot.";
        }
        return "";
    }
}
