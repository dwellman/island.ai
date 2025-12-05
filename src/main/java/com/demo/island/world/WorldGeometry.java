package com.demo.island.world;

/**
 * Defines canonical world bounds, island eligibility band, and spawn coordinate.
 */
public final class WorldGeometry {

    public enum Classification {
        OFF_WORLD,
        BOUNDARY,
        ISLAND_ELIGIBLE
    }

    // World envelope: x ∈ [-5,6], y ∈ [-1,9]
    public static final int WORLD_MIN_X = -5;
    public static final int WORLD_MAX_X = 6;
    public static final int WORLD_MIN_Y = -1;
    public static final int WORLD_MAX_Y = 9;

    // Island band: x ∈ [-4,5], y ∈ [0,8] (room for the cave corridor)
    public static final int ISLAND_MIN_X = -4;
    public static final int ISLAND_MAX_X = 5;
    public static final int ISLAND_MIN_Y = 0;
    public static final int ISLAND_MAX_Y = 8;

    // Spawn coordinate (canonical start)
    public static final Position SPAWN = new Position(0, 0);

    private WorldGeometry() {
    }

    public static Position apply(Position origin, Direction8 direction) {
        return origin.step(direction);
    }

    public static boolean isInsideWorldEnvelope(Position pos) {
        return pos.x() >= WORLD_MIN_X && pos.x() <= WORLD_MAX_X
                && pos.y() >= WORLD_MIN_Y && pos.y() <= WORLD_MAX_Y;
    }

    public static boolean isInsideIslandBand(Position pos) {
        return pos.x() >= ISLAND_MIN_X && pos.x() <= ISLAND_MAX_X
                && pos.y() >= ISLAND_MIN_Y && pos.y() <= ISLAND_MAX_Y;
    }

    public static Classification classify(Position pos) {
        if (!isInsideWorldEnvelope(pos)) {
            return Classification.OFF_WORLD;
        }
        if (isInsideIslandBand(pos)) {
            return Classification.ISLAND_ELIGIBLE;
        }
        return Classification.BOUNDARY;
    }
}
