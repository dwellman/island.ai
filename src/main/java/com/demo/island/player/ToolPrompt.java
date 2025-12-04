package com.demo.island.player;

public final class ToolPrompt {
    private final String intent;
    private final String args;
    private final String costHint;

    public ToolPrompt(String intent, String args, String costHint) {
        this.intent = intent;
        this.args = args;
        this.costHint = costHint;
    }

    public String intent() {
        return intent;
    }

    public String args() {
        return args;
    }

    public String costHint() {
        return costHint;
    }
}
