package com.demo.island.game;

import com.demo.island.world.CharacterThing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ChallengeResolver {

    private static final Logger LOG = LogManager.getLogger(ChallengeResolver.class);

    private final DiceService diceService;

    public ChallengeResolver(DiceService diceService) {
        this.diceService = diceService;
    }

    public ChallengeResult resolve(CharacterThing character, Challenge challenge) {
        int roll = diceService.rollD20();
        int abilityMod = character.getAbilityMods().getOrDefault(challenge.getAbility(), 0);
        int profApplied = 0;

        if (challenge.getChallengeType() == ChallengeType.SKILL_CHECK && challenge.isProficient()) {
            if (character.getSkillProficiencies().contains(challenge.getSkill())) {
                profApplied = character.getProficiencyBonus();
            }
        } else if (challenge.getChallengeType() == ChallengeType.SAVING_THROW && challenge.isProficient()) {
            if (character.getSaveProficiencies().contains(challenge.getAbility())) {
                profApplied = character.getProficiencyBonus();
            }
        } else if (challenge.getChallengeType() == ChallengeType.ABILITY_CHECK && challenge.isProficient()) {
            profApplied = character.getProficiencyBonus();
        }

        int total = roll + abilityMod + profApplied;
        boolean success = total >= challenge.getDc();
        boolean nat20 = roll == 20;
        boolean nat1 = roll == 1;
        int margin = total - challenge.getDc();
        ChallengeDegree degree = determineDegree(success, nat20, nat1);

        final int rollSnapshot = roll;
        final int modSnapshot = abilityMod;
        final int profSnapshot = profApplied;
        final int totalSnapshot = total;
        LOG.debug("[ROLL] id={} type={} ability={} skill={} roll={} mod={} prof={} total={} vs dc={} => {}",
                challenge.getChallengeId(),
                challenge.getChallengeType(),
                challenge.getAbility(),
                challenge.getSkill(),
                rollSnapshot,
                modSnapshot,
                profSnapshot,
                totalSnapshot,
                challenge.getDc(),
                degree);

        return new ChallengeResult(
                challenge.getChallengeId(),
                roll,
                abilityMod,
                profApplied,
                total,
                challenge.getDc(),
                success,
                nat20,
                nat1,
                margin,
                degree
        );
    }

    private static ChallengeDegree determineDegree(boolean success, boolean nat20, boolean nat1) {
        if (nat1) {
            return ChallengeDegree.CRIT_FAIL;
        }
        if (nat20) {
            return ChallengeDegree.CRIT_SUCCESS;
        }
        return success ? ChallengeDegree.SUCCESS : ChallengeDegree.FAIL;
    }
}
