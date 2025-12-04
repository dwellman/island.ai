package com.demo.island.engine.actions;

import com.demo.island.core.ActorEvent;
import com.demo.island.core.Creature;
import com.demo.island.core.ItemInstance;
import com.demo.island.core.Player;
import com.demo.island.core.TileEvent;
import com.demo.island.core.WorldState;
import com.demo.island.engine.FlagTarget;
import com.demo.island.engine.SetFlagChange;
import com.demo.island.engine.TransferItemChange;

public final class SkeletonMoveHook implements ItemActionHandler {

    private static final String VERB = "MOVE";

    @Override
    public String getHookId() {
        return StandardHooks.MOVE_REVEALS_CONTENTS_AWAKENS_GHOST;
    }

    @Override
    public boolean supportsVerb(String verb) {
        return VERB.equalsIgnoreCase(verb);
    }

    @Override
    public ItemActionResult apply(WorldState worldState, Player player, ItemInstance item) {
        String tileId = resolveTileId(worldState, player, item);
        ItemActionResult result = new ItemActionResult("You shove the skeleton aside. Something clatters free.");

        worldState.getItems().values().stream()
                .filter(i -> item.getItemId().equals(i.getContainedByItemId()))
                .forEach(contained -> result.addChange(
                        new TransferItemChange(contained.getItemId(), ItemInstance.OwnerKind.TILE, tileId)
                ));

        result.addChange(new SetFlagChange(FlagTarget.SESSION, null, "ghostAwakened", true));
        recordEvents(worldState, player, tileId);
        return result;
    }

    private String resolveTileId(WorldState worldState, Player player, ItemInstance item) {
        switch (item.getOwnerKind()) {
            case TILE:
                return item.getOwnerId();
            case PLAYER:
                Player owningPlayer = worldState.getPlayer(item.getOwnerId());
                return owningPlayer != null ? owningPlayer.getCurrentTileId() : player.getCurrentTileId();
            case CREATURE:
                Creature creature = worldState.getCreature(item.getOwnerId());
                return creature != null ? creature.getCurrentTileId() : player.getCurrentTileId();
            case ITEM:
            default:
                return player.getCurrentTileId();
        }
    }

    private void recordEvents(WorldState worldState, Player player, String tileId) {
        // Tile event for shared history
        if (worldState == null || tileId == null) {
            return;
        }
        var tile = worldState.getTile(tileId);
        if (tile == null) {
            return;
        }
        tile.recordEvent(new TileEvent(
                tileId,
                worldState.getSession().getTurnNumber(),
                player != null ? player.getPlayerId() : null,
                "PLAYER_MOVE_SKELETON",
                "Skeleton disturbed; something clattered free."
        ));

        // Actor event for the player
        if (player != null) {
            player.recordEvent(new ActorEvent(
                    worldState.getSession().getTurnNumber(),
                    tileId,
                    "PLAYER_MOVE_SKELETON",
                    null,
                    "You disturbed the skeleton; something clattered free."
            ));
        }
    }
}
