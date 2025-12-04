package com.demo.island.monkey;

public final class MonkeyIntentDto {

    private final String intent;
    private final String playerId;

    public MonkeyIntentDto(String intent, String playerId) {
        this.intent = intent;
        this.playerId = playerId;
    }

    public String getIntent() {
        return intent;
    }

    public String getPlayerId() {
        return playerId;
    }
}
