package com.demo.island.world;

public final class IslandMovement {

    private IslandMovement() {
    }

    public static MoveResult move(IslandMap map, PlayerLocation current, Direction8 direction, TurnClock clock) {
        Position origin = current.getPosition(map);
        Position targetPos = origin.step(direction);

        IslandTile target = map.get(targetPos).orElse(null);
        if (target == null) {
            return new MoveResult(MoveOutcome.MOVE_BLOCKED_NO_TILE, current, 0, false, GameOverReason.NONE);
        }

        WorldGeometry.Classification cls = WorldGeometry.classify(targetPos);
        if (cls == WorldGeometry.Classification.OFF_WORLD || cls == WorldGeometry.Classification.BOUNDARY) {
            return new MoveResult(MoveOutcome.MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY, current, 0, false, GameOverReason.NONE);
        }

        if (target.getSafety() == TileSafety.IMPOSSIBLE) {
            return new MoveResult(MoveOutcome.MOVE_BLOCKED_IMPOSSIBLE, current, 0, false, GameOverReason.NONE);
        }

        if (target.getSafety() == TileSafety.DEAD) {
            return new MoveResult(MoveOutcome.MOVE_FATAL_DEAD_TILE, current, 0, true, GameOverReason.DEAD_TILE);
        }

        int timeCost = target.getDifficulty().getTimeCost();
        clock.applyTimeCost(timeCost);
        boolean out = clock.isOutOfTime();
        GameOverReason reason = out ? GameOverReason.OUT_OF_TIME : GameOverReason.NONE;
        return new MoveResult(MoveOutcome.MOVE_OK, new PlayerLocation(target.getTileId()), timeCost, out, reason);
    }
}
