package com.demo.island.sim;

import com.demo.island.world.Direction8;
import com.demo.island.world.GameOverReason;
import com.demo.island.world.MoveOutcome;
import com.demo.island.world.PlantDensity;
import com.demo.island.world.PlantFamily;
import com.demo.island.world.Position;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TerrainFeature;
import com.demo.island.world.TileSafety;
import com.demo.island.world.TurnClock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A single step in the Gardener random walk over plots.
 */
public final class GardenerStepLog {

    private final int stepNumber;
    private final Direction8 direction;
    private final int turnBefore;
    private final int turnAfter;
    private final TurnClock.TimePhase phaseAfter;
    private final String plotId;
    private final Position position;
    private final TerrainDifficulty difficulty;
    private final TileSafety plotSafety;
    private final Set<TerrainFeature> terrainFeatures;
    private final PlantFamily primaryPlantFamily;
    private final List<PlantFamily> secondaryPlantFamilies;
    private final PlantDensity plantDensity;
    private final MoveOutcome moveOutcome;
    private final int timeCost;
    private final boolean gameOver;
    private final GameOverReason gameOverReason;
    private final String plotDescription;
    private final String stepNote;

    public GardenerStepLog(
            int stepNumber,
            Direction8 direction,
            int turnBefore,
            int turnAfter,
            TurnClock.TimePhase phaseAfter,
            String plotId,
            Position position,
            TerrainDifficulty difficulty,
            TileSafety plotSafety,
            Set<TerrainFeature> terrainFeatures,
            PlantFamily primaryPlantFamily,
            List<PlantFamily> secondaryPlantFamilies,
            PlantDensity plantDensity,
            MoveOutcome moveOutcome,
            int timeCost,
            boolean gameOver,
            GameOverReason gameOverReason,
            String plotDescription,
            String stepNote
    ) {
        this.stepNumber = stepNumber;
        this.direction = direction;
        this.turnBefore = turnBefore;
        this.turnAfter = turnAfter;
        this.phaseAfter = phaseAfter;
        this.plotId = plotId;
        this.position = position;
        this.difficulty = difficulty;
        this.plotSafety = plotSafety;
        this.terrainFeatures = terrainFeatures == null ? Set.of() : Set.copyOf(terrainFeatures);
        this.primaryPlantFamily = primaryPlantFamily;
        this.secondaryPlantFamilies = secondaryPlantFamilies == null ? List.of() : List.copyOf(secondaryPlantFamilies);
        this.plantDensity = plantDensity;
        this.moveOutcome = moveOutcome;
        this.timeCost = timeCost;
        this.gameOver = gameOver;
        this.gameOverReason = gameOverReason;
        this.plotDescription = plotDescription;
        this.stepNote = stepNote;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Direction8 getDirection() {
        return direction;
    }

    public int getTurnBefore() {
        return turnBefore;
    }

    public int getTurnAfter() {
        return turnAfter;
    }

    public TurnClock.TimePhase getPhaseAfter() {
        return phaseAfter;
    }

    public String getPlotId() {
        return plotId;
    }

    public Position getPosition() {
        return position;
    }

    public TerrainDifficulty getDifficulty() {
        return difficulty;
    }

    public TileSafety getPlotSafety() {
        return plotSafety;
    }

    public Set<TerrainFeature> getTerrainFeatures() {
        return Collections.unmodifiableSet(terrainFeatures);
    }

    public PlantFamily getPrimaryPlantFamily() {
        return primaryPlantFamily;
    }

    public List<PlantFamily> getSecondaryPlantFamilies() {
        return Collections.unmodifiableList(secondaryPlantFamilies);
    }

    public PlantDensity getPlantDensity() {
        return plantDensity;
    }

    public MoveOutcome getMoveOutcome() {
        return moveOutcome;
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

    public String getPlotDescription() {
        return plotDescription;
    }

    public String getStepNote() {
        return stepNote;
    }
}
