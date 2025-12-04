package com.demo.island.monkey;

import java.util.List;

public final class MonkeyDecisionDto {

    private final String narration;
    private final boolean turnConsumesTime;
    private final String dailyPhase;
    private final String targetTileId;
    private final List<MonkeyIntentDto> monkeyIntents;
    private final List<CheckRequestDto> checkRequests;
    private final List<String> hints;
    private final List<String> errors;

    public MonkeyDecisionDto(String narration, boolean turnConsumesTime, String dailyPhase, String targetTileId,
                             List<MonkeyIntentDto> monkeyIntents, List<CheckRequestDto> checkRequests,
                             List<String> hints, List<String> errors) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
        this.dailyPhase = dailyPhase;
        this.targetTileId = targetTileId;
        this.monkeyIntents = monkeyIntents == null ? List.of() : List.copyOf(monkeyIntents);
        this.checkRequests = checkRequests == null ? List.of() : List.copyOf(checkRequests);
        this.hints = hints == null ? List.of() : List.copyOf(hints);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
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

    public String getTargetTileId() {
        return targetTileId;
    }

    public List<MonkeyIntentDto> getMonkeyIntents() {
        return monkeyIntents;
    }

    public List<CheckRequestDto> getCheckRequests() {
        return checkRequests;
    }

    public List<String> getHints() {
        return hints;
    }

    public List<String> getErrors() {
        return errors;
    }

    public record CheckRequestDto(String checkType, String subjectKind, String subjectId, Integer difficulty) {
    }
}
