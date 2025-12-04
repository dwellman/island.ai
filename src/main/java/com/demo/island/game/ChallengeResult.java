package com.demo.island.game;

public final class ChallengeResult {
    private final String challengeId;
    private final int rollD20;
    private final int abilityMod;
    private final int proficiencyBonusApplied;
    private final int total;
    private final int dc;
    private final boolean success;
    private final boolean natural20;
    private final boolean natural1;
    private final int margin;
    private final ChallengeDegree degree;

    public ChallengeResult(String challengeId, int rollD20, int abilityMod, int proficiencyBonusApplied, int total, int dc,
                           boolean success, boolean natural20, boolean natural1, int margin, ChallengeDegree degree) {
        this.challengeId = challengeId;
        this.rollD20 = rollD20;
        this.abilityMod = abilityMod;
        this.proficiencyBonusApplied = proficiencyBonusApplied;
        this.total = total;
        this.dc = dc;
        this.success = success;
        this.natural20 = natural20;
        this.natural1 = natural1;
        this.margin = margin;
        this.degree = degree;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public int getRollD20() {
        return rollD20;
    }

    public int getAbilityMod() {
        return abilityMod;
    }

    public int getProficiencyBonusApplied() {
        return proficiencyBonusApplied;
    }

    public int getTotal() {
        return total;
    }

    public int getDc() {
        return dc;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isNatural20() {
        return natural20;
    }

    public boolean isNatural1() {
        return natural1;
    }

    public int getMargin() {
        return margin;
    }

    public ChallengeDegree getDegree() {
        return degree;
    }
}
