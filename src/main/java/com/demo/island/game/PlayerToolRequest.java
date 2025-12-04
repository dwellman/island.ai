package com.demo.island.game;

import com.demo.island.world.Direction8;

public final class PlayerToolRequest {
    private final PlayerTool tool;
    private final Direction8 direction;
    private final GameItemType itemType;

    public PlayerToolRequest(PlayerTool tool, Direction8 direction, GameItemType itemType) {
        this.tool = tool;
        this.direction = direction;
        this.itemType = itemType;
    }

    public static PlayerToolRequest look() {
        return new PlayerToolRequest(PlayerTool.LOOK, null, null);
    }

    public static PlayerToolRequest move(Direction8 direction) {
        return new PlayerToolRequest(PlayerTool.MOVE, direction, null);
    }

    public static PlayerToolRequest search() {
        return new PlayerToolRequest(PlayerTool.SEARCH, null, null);
    }

    public static PlayerToolRequest take(GameItemType item) {
        return new PlayerToolRequest(PlayerTool.TAKE, null, item);
    }

    public static PlayerToolRequest drop(GameItemType item) {
        return new PlayerToolRequest(PlayerTool.DROP, null, item);
    }

    public static PlayerToolRequest raftWork() {
        return new PlayerToolRequest(PlayerTool.RAFT_WORK, null, null);
    }

    public static PlayerToolRequest status() {
        return new PlayerToolRequest(PlayerTool.STATUS, null, null);
    }

    public PlayerTool getTool() {
        return tool;
    }

    public Direction8 getDirection() {
        return direction;
    }

    public GameItemType getItemType() {
        return itemType;
    }
}
