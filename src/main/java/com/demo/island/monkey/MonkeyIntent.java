package com.demo.island.monkey;

public final class MonkeyIntent {

    public enum IntentKind {
        FOLLOW_BANANA_CARRIER,
        STEAL_BANANA_FROM_PLAYER,
        THROW_POO_AT_PLAYER,
        HELP_VINE_HARVEST,
        RETURN_HOME_AND_SLEEP,
        IGNORE_PLAYERS
    }

    private final IntentKind intent;
    private final String playerId; // optional, e.g., target player

    public MonkeyIntent(IntentKind intent, String playerId) {
        this.intent = intent;
        this.playerId = playerId;
    }

    public IntentKind getIntent() {
        return intent;
    }

    public String getPlayerId() {
        return playerId;
    }

    public static MonkeyIntent of(IntentKind kind) {
        return new MonkeyIntent(kind, null);
    }

    public static MonkeyIntent targeting(IntentKind kind, String playerId) {
        return new MonkeyIntent(kind, playerId);
    }
}
