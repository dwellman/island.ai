package com.demo.island.ghost;

import com.demo.island.engine.check.CheckRequest;

import java.util.ArrayList;
import java.util.List;

public final class GhostDecision {

    private final String narration;
    private final boolean turnConsumesTime;
    private final List<GhostIntent> actions = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<CheckRequest> checkRequests = new ArrayList<>();

    public GhostDecision(String narration, boolean turnConsumesTime) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
    }

    public String getNarration() {
        return narration;
    }

    public boolean isTurnConsumesTime() {
        return turnConsumesTime;
    }

    public List<GhostIntent> getActions() {
        return actions;
    }

    public void addAction(GhostIntent intent) {
        actions.add(intent);
    }

    public List<String> getHints() {
        return hints;
    }

    public void addHint(String hint) {
        hints.add(hint);
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public List<CheckRequest> getCheckRequests() {
        return checkRequests;
    }

    public void addCheckRequest(CheckRequest request) {
        checkRequests.add(request);
    }
}
