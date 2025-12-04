package com.demo.island.game;

public final class AgentDecision {
    private final PlayerToolRequest request;
    private final String reason;
    private final AgentMood mood;
    private final String note;

    public AgentDecision(PlayerToolRequest request, String reason, AgentMood mood, String note) {
        this.request = request;
        this.reason = reason;
        this.mood = mood;
        this.note = note;
    }

    public PlayerToolRequest getRequest() {
        return request;
    }

    public String getReason() {
        return reason;
    }

    public AgentMood getMood() {
        return mood;
    }

    public String getNote() {
        return note;
    }
}
