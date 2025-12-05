package com.demo.island.sim;

import com.demo.island.world.IslandCreationResult;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandWorldBuilder;
import com.demo.island.world.TerrainDifficulty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringJoiner;

/**
 * Simple entrypoint to run the Gardener actor and dump human-readable logs.
 */
public final class GardenerRunner {

    private static final Logger LOG = LogManager.getLogger(GardenerRunner.class);

    private GardenerRunner() {
    }

    public static void main(String[] args) {
        int maxSteps = 200;
        long seed = 42L;
        if (args.length >= 1) {
            try {
                maxSteps = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        if (args.length >= 2) {
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        IslandCreationResult creation = IslandWorldBuilder.buildWorldWithLogging();
        IslandMap map = creation.getMap();
        // report already logged by builder

        GardenerRunResult result = GardenerSimulator.run(new GardenerRunConfig(maxSteps, seed), map);

        final int stepsCap = maxSteps;
        final long runSeed = seed;
        LOG.info("=== Gardener Run ===");
        LOG.info("maxSteps={} seed={}", stepsCap, runSeed);
        for (GardenerStepLog step : result.getSteps()) {
            String features = step.getTerrainFeatures().isEmpty()
                    ? "-"
                    : String.join(",", step.getTerrainFeatures().stream().map(Enum::name).toList());
            String plants = new StringJoiner(",")
                    .add(step.getPrimaryPlantFamily() == null ? "-" : step.getPrimaryPlantFamily().name())
                    .add(step.getPlantDensity() == null ? "-" : step.getPlantDensity().name())
                    .toString();
            LOG.debug(
                    "step={} dir={} turn={} -> {} phase={} plot={} pos=({}, {}) diff={} safety={} features={} flora={} outcome={} timeCost={} desc=\"{}\"",
                    step.getStepNumber(),
                    step.getDirection(),
                    step.getTurnBefore(),
                    step.getTurnAfter(),
                    step.getPhaseAfter(),
                    step.getPlotId(),
                    step.getPosition().x(),
                    step.getPosition().y(),
                    step.getDifficulty(),
                    step.getPlotSafety(),
                    features,
                    plants,
                    step.getMoveOutcome(),
                    step.getTimeCost(),
                    step.getPlotDescription()
            );
        }

        LOG.info("--- Summary ---");
        LOG.info("stepsTaken={}", result.getStepsTaken());
        LOG.info("finalPlotId={}", result.getFinalPlotId());
        LOG.info("finalTurn={} phase={}", result.getFinalTurnIndex(), result.getFinalPhase());
        LOG.info("gameOver={} reason={}", result.isGameOver(), result.getGameOverReason());
        LOG.info("uniquePlotsVisited={}", result.getUniquePlotsVisited());
        LOG.info(() -> "uniqueAnchorPlotsVisited=" + result.getUniqueAnchorPlotsVisited());
        for (TerrainDifficulty d : TerrainDifficulty.values()) {
            LOG.debug("entered_{}={}", d.name().toLowerCase(), result.getDifficultyEntries().getOrDefault(d, 0));
        }
        LOG.debug("blockedNoTile={}", result.getBlockedNoTileCount());
        LOG.debug("blockedBoundary={}", result.getBlockedBoundaryCount());
        LOG.debug("blockedImpossible={}", result.getBlockedImpossibleCount());
        LOG.debug("fatalDead={}", result.getFatalDeadCount());
    }
}
