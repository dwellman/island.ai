package com.demo.island.engine;

import com.demo.island.core.Player;
import com.demo.island.core.Tile;
import com.demo.island.core.WorldState;

public final class MovePlayerChange implements StateChange {

    private final String playerId;
    private final String targetTileId;

    public MovePlayerChange(String playerId, String targetTileId) {
        this.playerId = playerId;
        this.targetTileId = targetTileId;
    }

    @Override
    public void applyTo(WorldState worldState) {
        Player player = worldState.getPlayer(playerId);
        Tile tile = worldState.getTile(targetTileId);
        if (player != null && tile != null) {
            player.moveToTile(targetTileId);
            tile.setDiscovered(true);
        }
    }
}
