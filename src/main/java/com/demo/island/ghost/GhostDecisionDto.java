package com.demo.island.ghost;

import java.util.List;

public final class GhostDecisionDto {

    private final String narration;
    private final boolean turnConsumesTime;
    private final List<GhostIntentDto> actions;
    private final List<String> hints;
    private final List<String> errors;

    public GhostDecisionDto(String narration, boolean turnConsumesTime, List<GhostIntentDto> actions,
                            List<String> hints, List<String> errors) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
        this.actions = actions == null ? List.of() : List.copyOf(actions);
        this.hints = hints == null ? List.of() : List.copyOf(hints);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public String getNarration() {
        return narration;
    }

    public boolean isTurnConsumesTime() {
        return turnConsumesTime;
    }

    public List<GhostIntentDto> getActions() {
        return actions;
    }

    public List<String> getHints() {
        return hints;
    }

    public List<String> getErrors() {
        return errors;
    }
}
