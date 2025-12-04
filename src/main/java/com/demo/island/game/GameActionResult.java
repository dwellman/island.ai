package com.demo.island.game;

public final class GameActionResult {
    private final boolean success;
    private final String message;
    private final TurnContext turnContext;

    public GameActionResult(boolean success, String message, TurnContext turnContext) {
        this.success = success;
        this.message = message;
        this.turnContext = turnContext;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public TurnContext getTurnContext() {
        return turnContext;
    }
}
