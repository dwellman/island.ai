package com.demo.island.sim;

import com.demo.island.world.GardenerVisit;
import com.demo.island.world.IslandCreationResult;
import com.demo.island.world.IslandWorldBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Probe: how long to explore the island if each move costs 5 minutes.
 * Uses the Gardener coverage path from world build.
 */
public final class IslandClockProbeRunner {

    private static final Logger LOG = LogManager.getLogger(IslandClockProbeRunner.class);

    private IslandClockProbeRunner() {
    }

    public static void main(String[] args) {
        IslandCreationResult creation = IslandWorldBuilder.buildWorldWithLogging();
        var gardener = creation.getReport().gardener;
        var path = gardener.coveragePath;
        final int minutesPerMove = 5;
        LOG.info("=== Clock Probe: Full Island Exploration (5 min per move) ===");
        int turn = 0;
        for (GardenerVisit visit : path) {
            turn++;
            final int currentTurn = turn;
            final int totalMinutes = currentTurn * minutesPerMove;
            LOG.debug("visit={} plot={} pos=({}, {}) turn={} minutes={}",
                    visit.getIndex(), visit.getPlotId(), visit.getPosition().x(), visit.getPosition().y(), currentTurn, totalMinutes);
        }
        int totalMinutes = path.size() * minutesPerMove;
        double hours = totalMinutes / 60.0;
        LOG.info("Exploration path length={} moves, totalMinutes={}, approxHours={}",
                path.size(), totalMinutes, String.format(java.util.Locale.ROOT, "%.2f", hours));
    }
}
