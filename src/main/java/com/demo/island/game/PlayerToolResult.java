package com.demo.island.game;

public final class PlayerToolResult {
    private final String text;
    private final PlayerToolState state;

    public PlayerToolResult(String text, PlayerToolState state) {
        this.text = text;
        this.state = state;
    }

    public String getText() {
        return text;
    }

    public PlayerToolState getState() {
        return state;
    }
}
