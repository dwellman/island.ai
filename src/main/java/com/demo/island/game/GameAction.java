package com.demo.island.game;

import com.demo.island.world.Direction8;

public final class GameAction {
    private final GameActionType type;
    private final Direction8 direction;
    private final GameItemType itemType;

    private GameAction(GameActionType type, Direction8 direction, GameItemType itemType) {
        this.type = type;
        this.direction = direction;
        this.itemType = itemType;
    }

    public static GameAction move(GameActionType type, Direction8 dir) {
        return new GameAction(type, dir, null);
    }

    public static GameAction simple(GameActionType type) {
        return new GameAction(type, null, null);
    }

    public static GameAction withItem(GameActionType type, GameItemType item) {
        return new GameAction(type, null, item);
    }

    public GameActionType getType() {
        return type;
    }

    public Direction8 getDirection() {
        return direction;
    }

    public GameItemType getItemType() {
        return itemType;
    }
}
