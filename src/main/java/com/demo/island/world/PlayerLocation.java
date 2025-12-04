package com.demo.island.world;

/**
 * Simple player location anchored to a known anchor tile.
 */
public final class PlayerLocation {

    private final String tileId;

    public PlayerLocation(String tileId) {
        this.tileId = tileId;
    }

    public static PlayerLocation spawn() {
        return new PlayerLocation(AnchorTiles.startTile().getTileId());
    }

    public String getTileId() {
        return tileId;
    }

    public Position getPosition(IslandMap map) {
        return map.get(tileId)
                .map(IslandTile::getPosition)
                .orElseGet(() -> AnchorTiles.byId(tileId)
                        .map(AnchorTile::getPosition)
                        .orElseThrow(() -> new IllegalStateException("Unknown tile: " + tileId)));
    }

    public Position getPosition() {
        return AnchorTiles.byId(tileId)
                .map(AnchorTile::getPosition)
                .orElseThrow(() -> new IllegalStateException("Unknown tile: " + tileId));
    }
}
