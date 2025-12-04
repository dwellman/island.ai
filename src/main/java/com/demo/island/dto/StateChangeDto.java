package com.demo.island.dto;

public final class StateChangeDto {

    public enum Kind {
        MOVE_PLAYER,
        TRANSFER_ITEM,
        SET_FLAG,
        RUN_ITEM_HOOK,
        CHECK
    }

    private final Kind kind;
    private final String playerId;
    private final String targetTileId;
    private final String itemId;
    private final String ownerKind;
    private final String ownerId;
    private final String containedByItemId;
    private final String flagTarget;
    private final String flagName;
    private final boolean flagValue;
    private final String hookId;
    private final String checkType;
    private final String checkSubjectKind;
    private final String checkSubjectId;
    private final Integer difficulty;

    private StateChangeDto(Kind kind, String playerId, String targetTileId, String itemId, String ownerKind,
                           String ownerId, String containedByItemId, String flagTarget, String flagName, boolean flagValue,
                           String hookId, String checkType, String checkSubjectKind, String checkSubjectId, Integer difficulty) {
        this.kind = kind;
        this.playerId = playerId;
        this.targetTileId = targetTileId;
        this.itemId = itemId;
        this.ownerKind = ownerKind;
        this.ownerId = ownerId;
        this.containedByItemId = containedByItemId;
        this.flagTarget = flagTarget;
        this.flagName = flagName;
        this.flagValue = flagValue;
        this.hookId = hookId;
        this.checkType = checkType;
        this.checkSubjectKind = checkSubjectKind;
        this.checkSubjectId = checkSubjectId;
        this.difficulty = difficulty;
    }

    public Kind getKind() {
        return kind;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getTargetTileId() {
        return targetTileId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getOwnerKind() {
        return ownerKind;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getContainedByItemId() {
        return containedByItemId;
    }

    public String getFlagTarget() {
        return flagTarget;
    }

    public String getFlagName() {
        return flagName;
    }

    public boolean isFlagValue() {
        return flagValue;
    }

    public String getHookId() {
        return hookId;
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

    public static StateChangeDto movePlayer(String playerId, String targetTileId) {
        return new StateChangeDto(Kind.MOVE_PLAYER, playerId, targetTileId, null, null, null, null, null, null, false, null, null, null, null, null);
    }

    public static StateChangeDto transferItem(String itemId, String ownerKind, String ownerId, String containedByItemId) {
        return new StateChangeDto(Kind.TRANSFER_ITEM, null, null, itemId, ownerKind, ownerId, containedByItemId, null, null, false, null, null, null, null, null);
    }

    public static StateChangeDto setFlag(String flagTarget, String flagName, boolean value) {
        return new StateChangeDto(Kind.SET_FLAG, null, null, null, null, null, null, flagTarget, flagName, value, null, null, null, null, null);
    }

    public static StateChangeDto runItemHook(String hookId, String itemId) {
        return new StateChangeDto(Kind.RUN_ITEM_HOOK, null, null, itemId, null, null, null, null, null, false, hookId, null, null, null, null);
    }

    public static StateChangeDto check(String checkType, String subjectKind, String subjectId, int difficulty) {
        return new StateChangeDto(Kind.CHECK, null, null, null, null, null, null, null, null, false, null,
                checkType, subjectKind, subjectId, difficulty);
    }
}
