package com.demo.island.game;

import com.demo.island.world.Direction8;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerToolEngineTest {

    @Test
    void lookMoveSearchSmoke() {
        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);

        PlayerToolResult look = engine.invoke(PlayerToolRequest.look());
        assertThat(look.getText()).contains("[");
        assertThat(look.getState().locationId).isEqualTo(session.getLocation().getTileId());

        PlayerToolResult move = engine.invoke(PlayerToolRequest.move(Direction8.N));
        assertThat(move.getState().locationId).isEqualTo("T_CAMP");

        PlayerToolResult search = engine.invoke(PlayerToolRequest.search());
        assertThat(search.getState().locationId).isEqualTo("T_CAMP");
        assertThat(search.getText()).contains("[");
    }
}
