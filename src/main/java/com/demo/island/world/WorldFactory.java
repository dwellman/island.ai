package com.demo.island.world;

import com.demo.island.core.WorldState;

/**
 * Backward-compatible entrypoint to build the canonical world.
 */
public final class WorldFactory {

    private WorldFactory() {
    }

    public static WorldState createDemoWorld(String sessionId) {
        return WorldBuilder.build(sessionId);
    }
}
