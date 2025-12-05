package com.demo.island.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DmAgentHookTest {

    @AfterEach
    void reset() {
        DmAgentConfig.setEnabledOverride(null);
        PlayerToolEngine.resetDmAgentForTests();
    }

    @Test
    void overrideUsedWhenEnabled() {
        DmAgentConfig.setEnabledOverride(true);
        RecordingDmAgent agent = new RecordingDmAgent("Test override DM line.");
        PlayerToolEngine.setDmAgentForTests(agent);

        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);
        PlayerToolResult result = engine.invoke(PlayerToolRequest.look());

        assertThat(result.getText()).contains("Test override DM line.");
        assertThat(agent.lastContext).isNotNull();
        assertThat(agent.lastContext.actionOutcome().toolName()).isEqualTo("LOOK");
        assertThat(agent.lastContext.plot().visibleItems()).contains("rusty hatchet");
        assertThat(agent.lastContext.player().inventory()).isEmpty();
        assertThat(agent.lastContext.ghost().present()).isFalse();
    }

    @Test
    void nullOverrideFallsBackToCoreText() {
        String baseline = runLook();
        DmAgentConfig.setEnabledOverride(true);
        RecordingDmAgent agent = new RecordingDmAgent(null);
        PlayerToolEngine.setDmAgentForTests(agent);

        PlayerToolResult result = runLookResult();

        assertThat(result.getText()).isEqualTo(baseline);
    }

    @Test
    void disabledFlagKeepsCoreText() {
        String baseline = runLook();
        DmAgentConfig.setEnabledOverride(false);
        RecordingDmAgent agent = new RecordingDmAgent("Should not appear");
        PlayerToolEngine.setDmAgentForTests(agent);

        PlayerToolResult result = runLookResult();

        assertThat(result.getText()).isEqualTo(baseline);
    }

    private static final class RecordingDmAgent implements DmAgent {
        private final String response;
        private DmAgentContext lastContext;

        private RecordingDmAgent(String response) {
            this.response = response;
        }

        @Override
        public String rewrite(DmAgentContext context) {
            this.lastContext = context;
            return response;
        }
    }

    private PlayerToolResult runLookResult() {
        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);
        return engine.invoke(PlayerToolRequest.look());
    }

    private String runLook() {
        return runLookResult().getText();
    }
}
