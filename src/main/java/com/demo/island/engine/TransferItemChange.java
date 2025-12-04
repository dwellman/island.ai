package com.demo.island.engine;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.Player;
import com.demo.island.core.WorldState;

public final class TransferItemChange implements StateChange {

    private final String itemId;
    private final ItemInstance.OwnerKind newOwnerKind;
    private final String newOwnerId;
    private final String containedByItemId;

    public TransferItemChange(String itemId, ItemInstance.OwnerKind newOwnerKind, String newOwnerId) {
        this(itemId, newOwnerKind, newOwnerId, null);
    }

    public TransferItemChange(String itemId, ItemInstance.OwnerKind newOwnerKind, String newOwnerId, String containedByItemId) {
        this.itemId = itemId;
        this.newOwnerKind = newOwnerKind;
        this.newOwnerId = newOwnerId;
        this.containedByItemId = containedByItemId;
    }

    @Override
    public void applyTo(WorldState worldState) {
        ItemInstance item = worldState.getItem(itemId);
        if (item == null) {
            return;
        }

        // remove from previous player inventory if needed
        if (item.getOwnerKind() == ItemInstance.OwnerKind.PLAYER) {
            Player prevOwner = worldState.getPlayer(item.getOwnerId());
            if (prevOwner != null) {
                prevOwner.removeItem(itemId);
            }
        }

        item.changeOwner(newOwnerKind, newOwnerId);
        if (containedByItemId != null) {
            item.setContainedByItemId(containedByItemId);
        }

        if (newOwnerKind == ItemInstance.OwnerKind.PLAYER) {
            Player newOwner = worldState.getPlayer(newOwnerId);
            if (newOwner != null) {
                newOwner.addItem(itemId);
            }
        }
    }
}
