package com.demo.island.game;

/**
 * Describes a single ability/skill/save challenge.
 */
public final class Challenge {
    private final String challengeId;
    private final ChallengeType challengeType;
    private final Ability ability;
    private final Skill skill;
    private final int dc;
    private final boolean proficient;
    private final String description;

    public Challenge(String challengeId, ChallengeType challengeType, Ability ability, Skill skill, int dc, boolean proficient, String description) {
        this.challengeId = challengeId;
        this.challengeType = challengeType;
        this.ability = ability;
        this.skill = skill;
        this.dc = dc;
        this.proficient = proficient;
        this.description = description;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public ChallengeType getChallengeType() {
        return challengeType;
    }

    public Ability getAbility() {
        return ability;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getDc() {
        return dc;
    }

    public boolean isProficient() {
        return proficient;
    }

    public String getDescription() {
        return description;
    }
}
