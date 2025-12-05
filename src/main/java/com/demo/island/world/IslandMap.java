package com.demo.island.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds island tiles keyed by position and id.
 */
public final class IslandMap {

    private final Map<Position, IslandTile> byPosition = new HashMap<>();
    private final Map<String, IslandTile> byId = new HashMap<>();

    public void put(IslandTile tile) {
        IslandTile previous = byPosition.put(tile.getPosition(), tile);
        if (previous != null && !previous.getTileId().equals(tile.getTileId())) {
            byId.remove(previous.getTileId());
        }
        byId.put(tile.getTileId(), tile);
    }

    public Optional<IslandTile> get(Position position) {
        return Optional.ofNullable(byPosition.get(position));
    }

    public Optional<IslandTile> get(String tileId) {
        return Optional.ofNullable(byId.get(tileId));
    }

    public Collection<IslandTile> allTiles() {
        return Collections.unmodifiableCollection(byPosition.values());
    }
}
