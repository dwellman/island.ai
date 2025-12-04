package com.demo.island.game;

import com.demo.island.world.CharacterThing;
import com.demo.island.world.ThingKind;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ChallengeResolverTest {

    private CharacterThing buildHero() {
        CharacterThing c = new CharacterThing("hero", "Hero", ThingKind.CHARACTER, "T_WRECK_BEACH");
        c.getAbilityScores().put(Ability.STR, 16); // mod +3
        c.getAbilityScores().put(Ability.DEX, 12); // mod +1
        c.computeModsFromScores();
        c.setProficiencyBonus(2);
        c.getSkillProficiencies().add(Skill.ATHLETICS);
        c.getSaveProficiencies().add(Ability.STR);
        return c;
    }

    @Test
    void resolvesSkillCheckWithProficiency() {
        DiceService dice = new DiceService(new Random(42)); // deterministic
        ChallengeResolver resolver = new ChallengeResolver(dice);
        CharacterThing hero = buildHero();

        Challenge challenge = new Challenge("JUMP_CREEK", ChallengeType.SKILL_CHECK, Ability.STR, Skill.ATHLETICS, 12, true, "Jumping a creek.");
        ChallengeResult result = resolver.resolve(hero, challenge);

        // Seed 42 -> first d20 roll is predictable (random(20)+1 with java.util.Random)
        assertThat(result.getRollD20()).isBetween(1, 20);
        assertThat(result.getAbilityMod()).isEqualTo(3);
        assertThat(result.getProficiencyBonusApplied()).isEqualTo(2);
        assertThat(result.getTotal()).isEqualTo(result.getRollD20() + 5);
    }

    @Test
    void natural20IsCritSuccessEvenIfDcHigh() {
        DiceService dice = new DiceService(new Random() {
            @Override
            public int nextInt(int bound) {
                return bound - 1; // force 19 -> roll 20
            }
        });
        ChallengeResolver resolver = new ChallengeResolver(dice);
        CharacterThing hero = buildHero();
        Challenge challenge = new Challenge("IMPOSSIBLE", ChallengeType.ABILITY_CHECK, Ability.DEX, null, 50, false, "Impossible task.");

        ChallengeResult result = resolver.resolve(hero, challenge);
        assertThat(result.isNatural20()).isTrue();
        assertThat(result.getDegree()).isEqualTo(ChallengeDegree.CRIT_SUCCESS);
    }

    @Test
    void naturalOneIsCritFailEvenIfTotalHigh() {
        DiceService dice = new DiceService(new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // force 1
            }
        });
        ChallengeResolver resolver = new ChallengeResolver(dice);
        CharacterThing hero = buildHero();
        Challenge challenge = new Challenge("TRIP", ChallengeType.ABILITY_CHECK, Ability.STR, null, 5, false, "Simple task.");

        ChallengeResult result = resolver.resolve(hero, challenge);
        assertThat(result.isNatural1()).isTrue();
        assertThat(result.getDegree()).isEqualTo(ChallengeDegree.CRIT_FAIL);
    }
}
