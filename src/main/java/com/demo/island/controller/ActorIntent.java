package com.demo.island.controller;

public final class ActorIntent {

    public enum Kind {
        PLAYER_COMMAND,
        GHOST_TURN,
        MONKEY_TURN
    }

    private final Kind kind;
    private final String payload;

    public ActorIntent(Kind kind, String payload) {
        this.kind = kind;
        this.payload = payload;
    }

    public Kind getKind() {
        return kind;
    }

    public String getPayload() {
        return payload;
    }
}
