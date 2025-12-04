package com.demo.island.world;

import com.demo.island.game.GameItemType;

public final class ItemThing extends Thing {

    private final GameItemType itemType;
    private int stackSize;
    private String carriedByCharacterId;

    public ItemThing(String id, String name, GameItemType itemType, String currentPlotId) {
        super(id, name, ThingKind.ITEM, currentPlotId);
        this.itemType = itemType;
        this.stackSize = 1;
    }

    public GameItemType getItemType() {
        return itemType;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public String getCarriedByCharacterId() {
        return carriedByCharacterId;
    }

    public void setCarriedByCharacterId(String carriedByCharacterId) {
        this.carriedByCharacterId = carriedByCharacterId;
    }
}
