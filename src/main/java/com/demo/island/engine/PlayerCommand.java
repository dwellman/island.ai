package com.demo.island.engine;

public final class PlayerCommand {

    private final String playerId;
    private final String rawText;

    public PlayerCommand(String playerId, String rawText) {
        this.playerId = playerId;
        this.rawText = rawText;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getRawText() {
        return rawText;
    }
}
