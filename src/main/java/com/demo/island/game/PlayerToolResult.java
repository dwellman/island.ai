package com.demo.island.game;

public final class PlayerToolResult {
    private final String text;
    private final PlayerToolState state;
    private final TurnContext turnContext;

    public PlayerToolResult(String text, PlayerToolState state, TurnContext turnContext) {
        this.text = text;
        this.state = state;
        this.turnContext = turnContext;
    }

    public String getText() {
        return text;
    }

    public PlayerToolState getState() {
        return state;
    }

    public TurnContext getTurnContext() {
        return turnContext;
    }
}
