package com.demo.island.engine;

import java.util.ArrayList;
import java.util.List;

public final class DmDecision {

    private final String narration;
    private final boolean turnConsumesTime;
    private final List<StateChange> stateChanges = new ArrayList<>();
    private final List<com.demo.island.engine.check.CheckResult> checkResults = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public DmDecision(String narration, boolean turnConsumesTime) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
    }

    public String getNarration() {
        return narration;
    }

    public boolean isTurnConsumesTime() {
        return turnConsumesTime;
    }

    public List<StateChange> getStateChanges() {
        return stateChanges;
    }

    public void addStateChange(StateChange stateChange) {
        stateChanges.add(stateChange);
    }

    public List<com.demo.island.engine.check.CheckResult> getCheckResults() {
        return checkResults;
    }

    public void addCheckResult(com.demo.island.engine.check.CheckResult result) {
        checkResults.add(result);
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
}
