package com.demo.island.dto;

public final class CheckResultDto {

    private final String checkId;
    private final String type;
    private final String subjectKind;
    private final String subjectId;
    private final int dc;
    private final int roll;
    private final int modifier;
    private final int total;
    private final boolean success;

    public CheckResultDto(String checkId, String type, String subjectKind, String subjectId, int dc, int roll, int modifier, int total, boolean success) {
        this.checkId = checkId;
        this.type = type;
        this.subjectKind = subjectKind;
        this.subjectId = subjectId;
        this.dc = dc;
        this.roll = roll;
        this.modifier = modifier;
        this.total = total;
        this.success = success;
    }

    public String getCheckId() {
        return checkId;
    }

    public String getType() {
        return type;
    }

    public String getSubjectKind() {
        return subjectKind;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public int getDc() {
        return dc;
    }

    public int getDifficulty() {
        return dc;
    }

    public int getRoll() {
        return roll;
    }

    public int getModifier() {
        return modifier;
    }

    public int getTotal() {
        return total;
    }

    public boolean isSuccess() {
        return success;
    }
}
