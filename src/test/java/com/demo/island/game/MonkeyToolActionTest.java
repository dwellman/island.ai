package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.world.CharacterThing;
import com.demo.island.world.Direction8;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonkeyToolActionTest {

    @Test
    void monkeyMoveUpdatesMonkeyLocationOnly() {
        GameSession session = GameSession.newSession();
        CharacterThing monkey = findMonkey(session);
        String startPlot = monkey.getCurrentPlotId();
        PlotContext plot = ContextBuilder.buildPlotContext(session, startPlot);
        Direction8 dir = plot.neighborSummaries.keySet().iterator().next();

        PlayerToolRequest req = PlayerToolRequest.move(dir);
        ToolContext ctx = ToolActionExecutor.buildContext(session, plot, req, dir.name(), "Explore", "CURIOUS", "", monkey.getId());
        ToolOutcome outcome = new ToolActionExecutor().execute(ctx);

        assertThat(outcome.getOutcomeType()).isEqualTo(OutcomeType.SUCCESS);
        assertThat(monkey.getCurrentPlotId()).isNotEqualTo(startPlot);
        assertThat(session.getLocation().getTileId()).isEqualTo("T_WRECK_BEACH"); // player unchanged
    }

    @Test
    void monkeyLookSucceeds() {
        GameSession session = GameSession.newSession();
        CharacterThing monkey = findMonkey(session);
        PlotContext plot = ContextBuilder.buildPlotContext(session, monkey.getCurrentPlotId());

        PlayerToolRequest req = PlayerToolRequest.look();
        ToolContext ctx = ToolActionExecutor.buildContext(session, plot, req, "", "Observe", "CURIOUS", "", monkey.getId());
        ToolOutcome outcome = new ToolActionExecutor().execute(ctx);

        assertThat(outcome.getOutcomeType()).isEqualTo(OutcomeType.SUCCESS);
        assertThat(outcome.getDmText()).isNotBlank();
    }

    private CharacterThing findMonkey(GameSession session) {
        return session.getThingIndex().getAll().values().stream()
                .filter(t -> t instanceof CharacterThing ct && ct.getTags().contains("MONKEY_TROOP"))
                .map(t -> (CharacterThing) t)
                .findFirst()
                .orElseThrow();
    }
}
