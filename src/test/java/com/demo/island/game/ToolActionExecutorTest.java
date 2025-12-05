package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.world.Direction8;
import com.demo.island.world.ItemThing;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolActionExecutorTest {

    @Test
    void takeSuccessMovesItem() {
        GameSession session = GameSession.newSession();
        PlotContext plotContext = ContextBuilder.buildPlotContext(session);
        PlayerToolRequest req = PlayerToolRequest.take(GameItemType.HATCHET);
        ToolContext ctx = ToolActionExecutor.buildContext(session, plotContext, req, "rusty hatchet", "", "", "");
        ToolOutcome outcome = new ToolActionExecutor().execute(ctx);

        assertThat(outcome.getOutcomeType()).isEqualTo(OutcomeType.SUCCESS);
        assertThat(outcome.getReasonCode()).isEqualTo(ReasonCode.OK);
        assertThat(DmMessageMapper.messageFor(outcome, session.getClock().formatRemainingBracketed()))
                .contains("pick up");
        assertThat(session.getInventory().getOrDefault(GameItemType.HATCHET, 0)).isEqualTo(1);
        assertThat(ctx.thingIndex.getThingsInPlot("T_WRECK_BEACH"))
                .noneMatch(t -> t instanceof ItemThing it && it.getItemType() == GameItemType.HATCHET);
    }

    @Test
    void takeAlreadyCarryingBlocked() {
        GameSession session = GameSession.newSession();
        // First take succeeds
        PlotContext plotContext = ContextBuilder.buildPlotContext(session);
        PlayerToolRequest req = PlayerToolRequest.take(GameItemType.HATCHET);
        ToolContext ctx = ToolActionExecutor.buildContext(session, plotContext, req, "rusty hatchet", "", "", "");
        new ToolActionExecutor().execute(ctx);
        // Second take should block
        PlotContext plotContext2 = ContextBuilder.buildPlotContext(session);
        ToolContext ctx2 = ToolActionExecutor.buildContext(session, plotContext2, req, "rusty hatchet", "", "", "");
        ToolOutcome outcome = new ToolActionExecutor().execute(ctx2);

        assertThat(outcome.getOutcomeType()).isEqualTo(OutcomeType.BLOCKED);
        assertThat(outcome.getReasonCode()).isEqualTo(ReasonCode.ALREADY_CARRYING_ITEM);
        assertThat(DmMessageMapper.messageFor(outcome, session.getClock().formatRemainingBracketed()))
                .contains("already carrying");
        assertThat(session.getInventory().getOrDefault(GameItemType.HATCHET, 0)).isEqualTo(1);
    }

    @Test
    void moveInvalidDirectionBlocked() {
        GameSession session = GameSession.newSession();
        PlotContext plotContext = ContextBuilder.buildPlotContext(session);
        PlayerToolRequest req = PlayerToolRequest.move(Direction8.S); // off-map from start
        ToolContext ctx = ToolActionExecutor.buildContext(session, plotContext, req, "S", "", "", "");
        ToolOutcome outcome = new ToolActionExecutor().execute(ctx);

        assertThat(outcome.getOutcomeType()).isEqualTo(OutcomeType.BLOCKED);
        assertThat(outcome.getReasonCode()).isEqualTo(ReasonCode.NO_EXIT_IN_DIRECTION);
        assertThat(DmMessageMapper.messageFor(outcome, session.getClock().formatRemainingBracketed()).toLowerCase())
                .contains("can't go that way");
    }
}
