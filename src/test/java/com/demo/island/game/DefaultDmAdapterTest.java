package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.world.Direction8;
import com.demo.island.world.PlayerLocation;
import com.demo.island.world.TerrainDifficulty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDmAdapterTest {

    @Test
    void narratesIntroWhenNoAction() {
        DefaultDmAdapter dm = new DefaultDmAdapter();
        TurnContext ctx = new TurnContext();
        ctx.lastActionType = null;

        String body = dm.narrate(ctx);

        assertThat(body).contains("You are standing in the dark just before dawn");
    }

    @Test
    void narratesJumpSuccess() {
        DefaultDmAdapter dm = new DefaultDmAdapter();
        TurnContext ctx = new TurnContext();
        ctx.lastActionType = GameActionType.JUMP;
        ctx.lastChallenge = new Challenge("JUMP_GENERIC", ChallengeType.SKILL_CHECK, Ability.DEX, Skill.ACROBATICS, 12, true, "");
        ctx.lastChallengeResult = new ChallengeResult("JUMP_GENERIC", 18, 2, 2, 22, 12, true, false, false, 10, ChallengeDegree.CRIT_SUCCESS);

        String body = dm.narrate(ctx);

        assertThat(body).contains("clear the gap");
    }

    @Test
    void narratesMonkeyPooFailure() {
        DefaultDmAdapter dm = new DefaultDmAdapter();
        TurnContext ctx = new TurnContext();
        ctx.lastActionType = GameActionType.MOVE_WALK;
        ctx.lastActionResultSummary = "You move N.";
        ctx.lastActionSuccess = true;
        ctx.lastChallenge = new Challenge("DODGE_MONKEY_POO", ChallengeType.SAVING_THROW, Ability.DEX, null, 12, true, "");
        ctx.lastChallengeResult = new ChallengeResult("DODGE_MONKEY_POO", 2, 2, 2, 6, 12, false, false, true, -6, ChallengeDegree.CRIT_FAIL);

        String body = dm.narrate(ctx);

        assertThat(body).contains("wet smack").contains("monkeys");
    }

    @Test
    void narratesLookWithPlotDescription() {
        DefaultDmAdapter dm = new DefaultDmAdapter();
        TurnContext ctx = new TurnContext();
        ctx.lastActionType = GameActionType.LOOK;
        PlotContext plot = new PlotContext();
        plot.currentDescription = "A sandy beach with scattered driftwood.";
        ctx.plotContext = plot;

        String body = dm.narrate(ctx);

        assertThat(body).contains("sandy beach");
    }
}
