package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnContextBuilderTest {

    @Test
    void buildsTurnContextWithTimeAndPlot() {
        GameSession session = GameSession.newSession();
        session.getClock().advance(120); // two hours

        GameAction action = GameAction.simple(GameActionType.LOOK);
        TurnContext ctx = TurnContextBuilder.build(session, action, "LOOK executed.", true, null, null);

        assertThat(ctx.timePrefix).isEqualTo(session.getClock().formatRemainingBracketed());
        assertThat(ctx.phase).isEqualTo(session.getClock().getPhase());
        assertThat(ctx.currentPlotId).isEqualTo(session.getLocation().getTileId());
        assertThat(ctx.plotContext).isInstanceOf(PlotContext.class);
        assertThat(ctx.lastActionType).isEqualTo(GameActionType.LOOK);
        assertThat(ctx.lastChallenge).isNull();
        assertThat(ctx.lastChallengeResult).isNull();
    }
}
