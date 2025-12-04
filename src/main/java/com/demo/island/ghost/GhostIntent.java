package com.demo.island.ghost;

public final class GhostIntent {

    public enum Verb {
        SET_TARGET_TILE,
        SET_FLAG,
        TRANSFER_ITEM,
        RUN_ITEM_HOOK,
        CHECK
    }

    private final Verb verb;
    private final String creatureId;
    private final String targetTileId;
    private final String flagTarget;
    private final String flagName;
    private final Boolean flagValue;
    private final String checkType;
    private final String checkSubjectKind;
    private final String checkSubjectId;
    private final Integer difficulty;

    public GhostIntent(Verb verb, String creatureId, String targetTileId, String flagTarget, String flagName, Boolean flagValue,
                       String checkType, String checkSubjectKind, String checkSubjectId, Integer difficulty) {
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

    public Verb getVerb() {
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

    public static GhostIntent setTargetTile(String creatureId, String targetTileId) {
        return new GhostIntent(Verb.SET_TARGET_TILE, creatureId, targetTileId, null, null, null,
                null, null, null, null);
    }

    public static GhostIntent check(String checkType, String subjectKind, String subjectId, int difficulty) {
        return new GhostIntent(Verb.CHECK, null, null, null, null, null, checkType, subjectKind, subjectId, difficulty);
    }

    public static GhostIntent setFlag(String flagTarget, String flagName, boolean value) {
        return new GhostIntent(Verb.SET_FLAG, null, null, flagTarget, flagName, value, null, null, null, null);
    }
}
