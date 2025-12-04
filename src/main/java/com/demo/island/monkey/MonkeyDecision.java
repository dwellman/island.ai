package com.demo.island.monkey;

import com.demo.island.engine.check.CheckRequest;

import java.util.ArrayList;
import java.util.List;

public final class MonkeyDecision {

    private final String narration;
    private final boolean turnConsumesTime;
    private String dailyPhase;
    private String targetTileId;
    private final List<MonkeyIntent> intents = new ArrayList<>();
    private final List<CheckRequest> checkRequests = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public MonkeyDecision(String narration, boolean turnConsumesTime) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
    }

    public String getNarration() {
        return narration;
    }

    public boolean isTurnConsumesTime() {
        return turnConsumesTime;
    }

    public String getDailyPhase() {
        return dailyPhase;
    }

    public void setDailyPhase(String dailyPhase) {
        this.dailyPhase = dailyPhase;
    }

    public String getTargetTileId() {
        return targetTileId;
    }

    public void setTargetTileId(String targetTileId) {
        this.targetTileId = targetTileId;
    }

    public List<MonkeyIntent> getIntents() {
        return intents;
    }

    public void addIntent(MonkeyIntent intent) {
        intents.add(intent);
    }

    public List<CheckRequest> getCheckRequests() {
        return checkRequests;
    }

    public void addCheckRequest(CheckRequest request) {
        checkRequests.add(request);
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
