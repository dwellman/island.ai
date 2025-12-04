package com.demo.island.game;

import com.demo.island.world.Direction8;
import com.demo.island.world.GameOverReason;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.Position;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TileSafety;
import com.demo.island.world.WorldGeometry;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class GameEngine {

    private static final Logger LOG = Logger.getLogger(GameEngine.class.getName());

    private GameEngine() {
    }

    public static String buildIntroMessage(CosmosClock clock) {
        return clock.formatRemainingBracketed() + " You are standing in the dark just before dawn. You have no idea how you got here.";
    }

    public static GameActionResult perform(GameSession session, GameAction action) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return new GameActionResult(false, prefix(session) + " Game is over.");
        }

        GameActionType type = action.getType();
        switch (type) {
            case MOVE_WALK, MOVE_RUN:
                return handleMove(session, action);
            case LOOK:
                applyTime(session, GameActionCost.timeCost(GameActionType.LOOK, currentDiff(session)));
                checkOutOfTime(session);
                return new GameActionResult(true, prefix(session) + " You take a quick look around.");
            case SEARCH:
                applyTime(session, GameActionCost.timeCost(GameActionType.SEARCH, currentDiff(session)));
                discover(session);
                checkOutOfTime(session);
                return new GameActionResult(true, prefix(session) + " You search the area.");
            case PICK_UP:
                return handlePickUp(session, action);
            case DROP:
                return handleDrop(session, action);
            case RAFT_WORK_SMALL:
                applyTime(session, GameActionCost.timeCost(GameActionType.RAFT_WORK_SMALL, currentDiff(session)));
                checkOutOfTime(session);
                return new GameActionResult(true, prefix(session) + " You tidy some rope and driftwood.");
            case RAFT_WORK_MAJOR:
                return handleRaftWork(session);
            case LAUNCH_RAFT:
                return handleLaunch(session);
            default:
                return new GameActionResult(false, prefix(session) + " Action not supported.");
        }
    }

    private static GameActionResult handleMove(GameSession session, GameAction action) {
        Direction8 dir = action.getDirection();
        if (dir == null) {
            return new GameActionResult(false, prefix(session) + " No direction provided.");
        }
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new GameActionResult(false, prefix(session) + " You seem to be nowhere.");
        }
        Position targetPos = current.getPosition().step(dir);
        IslandTile target = session.getMap().get(targetPos).orElse(null);
        if (target == null) {
            return new GameActionResult(false, prefix(session) + " No path that way.");
        }
        WorldGeometry.Classification cls = WorldGeometry.classify(targetPos);
        if (cls == WorldGeometry.Classification.OFF_WORLD || cls == WorldGeometry.Classification.BOUNDARY) {
            return new GameActionResult(false, prefix(session) + " The island ends that way.");
        }
        if (target.getSafety() == TileSafety.IMPOSSIBLE) {
            return new GameActionResult(false, prefix(session) + " Impassable terrain.");
        }
        if (target.getSafety() == TileSafety.DEAD) {
            // treat as blocked for now
            return new GameActionResult(false, prefix(session) + " That way looks deadly.");
        }
        int timeCost = GameActionCost.timeCost(action.getType(), target.getDifficulty());
        session.setLocation(new com.demo.island.world.PlayerLocation(target.getTileId()));
        applyTime(session, timeCost);
        checkOutOfTime(session);
        LOG.fine(() -> "Moved " + dir + " to " + target.getTileId() + " cost=" + timeCost + " totalPips=" + session.getClock().getTotalPips());
        return new GameActionResult(true, prefix(session) + " You move " + dir + ".");
    }

    private static GameActionResult handlePickUp(GameSession session, GameAction action) {
        GameItemType item = action.getItemType();
        if (item == null) {
            return new GameActionResult(false, prefix(session) + " Pick up what?");
        }
        PlotResources res = session.getPlotResources().get(session.getLocation().getTileId());
        if (res == null || !res.isDiscovered()) {
            return new GameActionResult(false, prefix(session) + " You haven't found any items here.");
        }
        if (!res.has(item)) {
            return new GameActionResult(false, prefix(session) + " No such item here.");
        }
        res.take(item);
        session.getInventory().put(item, session.getInventory().getOrDefault(item, 0) + 1);
        applyTime(session, GameActionCost.timeCost(GameActionType.PICK_UP, currentDiff(session)));
        checkOutOfTime(session);
        return new GameActionResult(true, prefix(session) + " Picked up " + item);
    }

    private static GameActionResult handleDrop(GameSession session, GameAction action) {
        GameItemType item = action.getItemType();
        if (item == null) {
            return new GameActionResult(false, prefix(session) + " Drop what?");
        }
        int invCount = session.getInventory().getOrDefault(item, 0);
        if (invCount <= 0) {
            return new GameActionResult(false, prefix(session) + " You don't have that.");
        }
        session.getInventory().put(item, invCount - 1);
        PlotResources res = session.getPlotResources().computeIfAbsent(session.getLocation().getTileId(), k -> new PlotResources(true));
        res.drop(item);
        applyTime(session, GameActionCost.timeCost(GameActionType.DROP, currentDiff(session)));
        checkOutOfTime(session);
        return new GameActionResult(true, prefix(session) + " Dropped " + item);
    }

    private static GameActionResult handleRaftWork(GameSession session) {
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new GameActionResult(false, prefix(session) + " Unknown location.");
        }
        String id = current.getTileId();
        if (!id.equals("T_CAMP") && !id.equals("T_WRECK_BEACH")) {
            return new GameActionResult(false, prefix(session) + " You need a stable site to work on the raft.");
        }
        int progress = session.getRaftProgress();
        Map<GameItemType, Integer> need = requirementsFor(progress);
        if (need.isEmpty()) {
            return new GameActionResult(false, prefix(session) + " Raft is already complete.");
        }
        if (!hasInventory(session, need)) {
            return new GameActionResult(false, prefix(session) + " You lack materials: " + need);
        }
        consumeInventory(session, need);
        session.incrementRaftProgress();
        applyTime(session, GameActionCost.timeCost(GameActionType.RAFT_WORK_MAJOR, currentDiff(session)));
        checkOutOfTime(session);
        return new GameActionResult(true, prefix(session) + " Raft work advanced to step " + session.getRaftProgress());
    }

    private static GameActionResult handleLaunch(GameSession session) {
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new GameActionResult(false, prefix(session) + " Unknown location.");
        }
        if (!current.getTileId().equals("T_WRECK_BEACH")) {
            return new GameActionResult(false, prefix(session) + " You need to launch at the beach.");
        }
        if (!session.isRaftReady()) {
            return new GameActionResult(false, prefix(session) + " The raft is not ready.");
        }
        if (session.getClock().isOutOfTime()) {
            return new GameActionResult(false, prefix(session) + " Too late to launch.");
        }
        applyTime(session, 5);
        session.setStatus(GameStatus.WON);
        session.setGameEndReason(GameEndReason.RAFT_LAUNCHED);
        return new GameActionResult(true, prefix(session) + " You push off and launch the raft!");
    }

    private static Map<GameItemType, Integer> requirementsFor(int progress) {
        Map<GameItemType, Integer> req = new EnumMap<>(GameItemType.class);
        switch (progress) {
            case 0 -> req.put(GameItemType.WOOD_LOG, 3);
            case 1 -> {
                req.put(GameItemType.WOOD_LOG, 2);
                req.put(GameItemType.VINE_ROPE, 2);
            }
            case 2 -> {
                req.put(GameItemType.METAL_SCRAP, 1);
                req.put(GameItemType.VINE_ROPE, 1);
            }
            default -> {
            }
        }
        return req;
    }

    private static boolean hasInventory(GameSession session, Map<GameItemType, Integer> need) {
        for (var entry : need.entrySet()) {
            if (session.getInventory().getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeInventory(GameSession session, Map<GameItemType, Integer> need) {
        need.forEach((k, v) -> session.getInventory().put(k, session.getInventory().getOrDefault(k, 0) - v));
    }

    private static void discover(GameSession session) {
        PlotResources res = session.getPlotResources().get(session.getLocation().getTileId());
        if (res != null) {
            res.setDiscovered(true);
        }
    }

    private static TerrainDifficulty currentDiff(GameSession session) {
        return session.getMap().get(session.getLocation().getTileId()).map(IslandTile::getDifficulty).orElse(TerrainDifficulty.NORMAL);
    }

    private static void applyTime(GameSession session, int pips) {
        if (pips > 0) {
            session.getClock().advance(pips);
        }
    }

    private static void checkOutOfTime(GameSession session) {
        if (session.getClock().isOutOfTime() && session.getStatus() != GameStatus.WON) {
            session.setStatus(GameStatus.LOST);
            session.setGameEndReason(GameEndReason.OUT_OF_TIME);
        }
    }

    private static String prefix(GameSession session) {
        return session.getClock().formatRemainingBracketed();
    }
}
