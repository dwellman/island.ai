package com.demo.island.game;

import com.demo.island.game.ghost.GhostAgent;
import com.demo.island.game.ghost.GhostAgentRegistry;
import com.demo.island.game.ghost.GhostManifestation;
import com.demo.island.game.ghost.GhostMode;
import com.demo.island.game.PlayerToolRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhostLlmHookTest {

    @AfterEach
    void reset() {
        DmAgentConfig.setEnabledOverride(null);
        PlayerToolEngine.resetDmAgentForTests();
        GhostAgentRegistry.reset();
    }

    @Test
    void ghostAgentModeAndTextPropagate() {
        GhostAgentRegistry.setEnabledOverride(true);
        GhostAgentRegistry.setAgent(new StubGhostAgent());
        DmAgentConfig.setEnabledOverride(true);
        RecordingDmAgent dm = new RecordingDmAgent();
        PlayerToolEngine.setDmAgentForTests(dm);

        GameSession session = GameSession.newSession();
        session.setLocation(new com.demo.island.world.PlayerLocation("T_OLD_RUINS"));
        PlayerToolEngine engine = new PlayerToolEngine(session);

        PlayerToolResult result = engine.invoke(PlayerToolRequest.look());

        assertThat(result.getTurnContext()).isNotNull();
        assertThat(result.getTurnContext().ghostEventTriggered).isTrue();
        assertThat(result.getTurnContext().ghostMode).isEqualTo(GhostMode.WHISPER.name());
        assertThat(result.getTurnContext().ghostText).isEqualTo("Test whisper.");
        assertThat(dm.lastContext).isNotNull();
        assertThat(dm.lastContext.ghost().mode()).isEqualTo(GhostMode.WHISPER.name());
        assertThat(dm.lastContext.ghost().text()).isEqualTo("Test whisper.");
    }

    private static final class StubGhostAgent implements GhostAgent {
        @Override
        public GhostManifestation manifest(com.demo.island.game.ghost.GhostState state) {
            return new GhostManifestation(GhostMode.WHISPER, "Test whisper.");
        }
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
