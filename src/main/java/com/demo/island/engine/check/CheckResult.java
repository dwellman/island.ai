package com.demo.island.engine.check;

public final class CheckResult {

    private final CheckRequest request;
    private final boolean success;
    private final int roll;
    private final int modifier;
    private final int total;
    private final String checkId;

    public CheckResult(CheckRequest request, boolean success, int roll, int modifier, int total, String checkId) {
        this.request = request;
        this.success = success;
        this.roll = roll;
        this.modifier = modifier;
        this.total = total;
        this.checkId = checkId;
    }

    public CheckRequest getRequest() {
        return request;
    }

    public boolean isSuccess() {
        return success;
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

    public String getCheckId() {
        return checkId;
    }
}
