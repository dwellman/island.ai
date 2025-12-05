package com.demo.island.game.monkey;

import com.demo.island.game.AgentMood;
import com.demo.island.game.PlayerToolRequest;

public final class MonkeyDecision {
    private final PlayerToolRequest request;
    private final String targetRaw;
    private final String reason;
    private final AgentMood mood;

    public MonkeyDecision(PlayerToolRequest request, String targetRaw, String reason, AgentMood mood) {
        this.request = request;
        this.targetRaw = targetRaw;
        this.reason = reason;
        this.mood = mood == null ? AgentMood.CURIOUS : mood;
    }

    public PlayerToolRequest request() {
        return request;
    }

    public String targetRaw() {
        return targetRaw;
    }

    public String reason() {
        return reason;
    }

    public AgentMood mood() {
        return mood;
    }
}
