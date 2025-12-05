package com.demo.island.game;

import com.demo.island.world.PlayerLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhostPresenceEventTest {

    @AfterEach
    void reset() {
        DmAgentConfig.setEnabledOverride(null);
        PlayerToolEngine.resetDmAgentForTests();
    }

    @Test
    void ghostPresenceTriggersOncePerAnchor() {
        DmAgentConfig.setEnabledOverride(true);
        RecordingDmAgent agent = new RecordingDmAgent();
        PlayerToolEngine.setDmAgentForTests(agent);

        GameSession session = GameSession.newSession();
        session.setLocation(new PlayerLocation("T_OLD_RUINS"));
        PlayerToolEngine engine = new PlayerToolEngine(session);

        PlayerToolResult first = engine.invoke(PlayerToolRequest.look());
        assertThat(first.getTurnContext()).isNotNull();
        assertThat(first.getTurnContext().ghostEventTriggered).isTrue();
        assertThat(first.getTurnContext().ghostEventPlotId).isEqualTo("T_OLD_RUINS");
        assertThat(first.getTurnContext().ghostEventText).isNotBlank();
        assertThat(session.getGhostAnchorsTriggered()).contains("T_OLD_RUINS");
        assertThat(agent.lastContext).isNotNull();
        assertThat(agent.lastContext.ghost().present()).isTrue();
        assertThat(agent.lastContext.ghost().plotId()).isEqualTo("T_OLD_RUINS");

        PlayerToolResult second = engine.invoke(PlayerToolRequest.look());
        assertThat(second.getTurnContext().ghostEventTriggered).isFalse();
        assertThat(session.getGhostAnchorsTriggered()).hasSize(1);
    }

    private static final class RecordingDmAgent implements DmAgent {
        private DmAgentContext lastContext;

        @Override
        public String rewrite(DmAgentContext context) {
            this.lastContext = context;
            return null;
        }
    }
}
