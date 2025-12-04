package com.demo.island.ghost;

public final class GhostIntentDto {

    private final String verb;
    private final String creatureId;
    private final String targetTileId;
    private final String flagTarget;
    private final String flagName;
    private final Boolean flagValue;
    private final String checkType;
    private final String checkSubjectKind;
    private final String checkSubjectId;
    private final Integer difficulty;

    public GhostIntentDto(String verb, String creatureId, String targetTileId, String flagTarget, String flagName,
                          Boolean flagValue, String checkType, String checkSubjectKind, String checkSubjectId, Integer difficulty) {
        this.verb = verb;
        this.creatureId = creatureId;
        this.targetTileId = targetTileId;
        this.flagTarget = flagTarget;
        this.flagName = flagName;
        this.flagValue = flagValue;
        this.checkType = checkType;
        this.checkSubjectKind = checkSubjectKind;
        this.checkSubjectId = checkSubjectId;
        this.difficulty = difficulty;
    }

    public String getVerb() {
        return verb;
    }

    public String getCreatureId() {
        return creatureId;
    }

    public String getTargetTileId() {
        return targetTileId;
    }

    public String getFlagTarget() {
        return flagTarget;
    }

    public String getFlagName() {
        return flagName;
    }

    public Boolean getFlagValue() {
        return flagValue;
    }

    public String getCheckType() {
        return checkType;
    }

    public String getCheckSubjectKind() {
        return checkSubjectKind;
    }

    public String getCheckSubjectId() {
        return checkSubjectId;
    }

    public Integer getDifficulty() {
        return difficulty;
    }
}
