package com.demo.island.engine.check;

public final class CheckRequest {

    private final CheckType type;
    private final CheckSubjectKind subjectKind;
    private final String subjectId;
    private final int difficulty;

    public CheckRequest(CheckType type, CheckSubjectKind subjectKind, String subjectId, int difficulty) {
        this.type = type;
        this.subjectKind = subjectKind;
        this.subjectId = subjectId;
        this.difficulty = difficulty;
    }

    public CheckType getType() {
        return type;
    }

    public CheckSubjectKind getSubjectKind() {
        return subjectKind;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
