package com.demo.island.game;

public final class GameActionResult {
    private final boolean success;
    private final String message;

    public GameActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
