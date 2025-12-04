package com.demo.island.sim;

import com.demo.island.game.CosmosClock;
import com.demo.island.game.GameAction;
import com.demo.island.game.GameActionResult;
import com.demo.island.game.GameActionType;
import com.demo.island.game.GameEndReason;
import com.demo.island.game.GameEngine;
import com.demo.island.game.GameSession;
import com.demo.island.game.GameStatus;
import com.demo.island.world.Direction8;
import com.demo.island.world.IslandTile;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TileSafety;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Game-layer probe that plays simple actions with a CosmosClock.
 */
public final class GardenerGameProbeRunner {

    private static final Logger LOG = Logger.getLogger(GardenerGameProbeRunner.class.getName());

    private GardenerGameProbeRunner() {
    }

    public static void main(String[] args) {
        GameSession session = GameSession.newSession();
        CosmosClock clock = session.getClock();
        int actionsTaken = 0;
        int actionCap = 500;
        GameEndReason reason = GameEndReason.NONE;

        LOG.info(() -> "=== Gardener Game Probe ===");
        LOG.info(() -> GameEngine.buildIntroMessage(clock));
        while (actionsTaken < actionCap && session.getStatus() == GameStatus.IN_PROGRESS) {
            IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElseThrow();
            GameActionType actionType = chooseAction(session, current);
            GameAction action = buildAction(session, current, actionType);

            GameActionResult result = GameEngine.perform(session, action);
            actionsTaken++;

            final GameActionType actionSnapshot = actionType;
            final CosmosClock.Phase phase = clock.getPhase();
            final int totalPips = clock.getTotalPips();
            final String plotIdSnapshot = session.getLocation().getTileId();
            final TerrainDifficulty diffSnapshot = session.getMap().get(plotIdSnapshot).orElseThrow().getDifficulty();
            LOG.fine(() -> "action=" + actionSnapshot
                    + " plot=" + plotIdSnapshot
                    + " diff=" + diffSnapshot
                    + " phase=" + phase
                    + " totalPips=" + totalPips
                    + " msg=" + result.getMessage());

            if (session.getStatus() == GameStatus.WON) {
                reason = GameEndReason.RAFT_LAUNCHED;
                break;
            }
            if (session.getStatus() == GameStatus.LOST) {
                reason = session.getGameEndReason();
                break;
            }
        }

        final int totalMinutes = clock.getTotalPips();
        final double hours = totalMinutes / 60.0;
        final int actionsSnapshot = actionsTaken;
        final String finalPlotSnapshot = session.getLocation().getTileId();
        final CosmosClock.Phase finalPhaseSnapshot = clock.getPhase();
        final GameEndReason reasonSnapshot = reason;
        LOG.info(() -> "Probe summary: actionsTaken=" + actionsSnapshot
                + " finalPlotId=" + finalPlotSnapshot
                + " totalPips=" + totalMinutes
                + " totalMinutes=" + totalMinutes
                + " totalHours=" + String.format(java.util.Locale.ROOT, "%.2f", hours)
                + " finalPhase=" + finalPhaseSnapshot
                + " gameOverReason=" + (reasonSnapshot == GameEndReason.NONE ? "CAP_REACHED" : reasonSnapshot));
    }

    private static GameAction buildAction(GameSession session, IslandTile current, GameActionType actionType) {
        if (actionType == GameActionType.MOVE_WALK || actionType == GameActionType.MOVE_RUN) {
            Optional<Direction8> dir = chooseMoveDirection(session.getMap(), current);
            return GameAction.move(actionType, dir.orElse(null));
        }
        return GameAction.simple(actionType);
    }

    private static GameActionType chooseAction(GameSession session, IslandTile current) {
        // Simple heuristic: move along PATH if possible, else move to any walkable neighbor, else LOOK.
        if (current.getFeatures().contains(com.demo.island.world.TerrainFeature.PATH)) {
            return GameActionType.MOVE_WALK;
        }
        for (Direction8 dir : Direction8.values()) {
            IslandTile neighbor = session.getMap().get(current.getPosition().step(dir)).orElse(null);
            if (neighbor != null && neighbor.getSafety() == TileSafety.NORMAL && neighbor.isWalkable()) {
                return GameActionType.MOVE_WALK;
            }
        }
        return GameActionType.LOOK;
    }

    private static Optional<Direction8> chooseMoveDirection(com.demo.island.world.IslandMap map, IslandTile current) {
        for (Direction8 dir : Direction8.values()) {
            IslandTile neighbor = map.get(current.getPosition().step(dir)).orElse(null);
            if (neighbor != null && neighbor.getFeatures().contains(com.demo.island.world.TerrainFeature.PATH)
                    && neighbor.getSafety() == TileSafety.NORMAL && neighbor.isWalkable()) {
                return Optional.of(dir);
            }
        }
        for (Direction8 dir : Direction8.values()) {
            IslandTile neighbor = map.get(current.getPosition().step(dir)).orElse(null);
            if (neighbor != null && neighbor.getSafety() == TileSafety.NORMAL && neighbor.isWalkable()) {
                return Optional.of(dir);
            }
        }
        return Optional.empty();
    }
}
