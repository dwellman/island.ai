package com.demo.island.world;

public enum MoveOutcome {
    MOVE_OK,
    MOVE_BLOCKED_NO_TILE,
    MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY,
    MOVE_BLOCKED_IMPOSSIBLE,
    MOVE_FATAL_DEAD_TILE
}
