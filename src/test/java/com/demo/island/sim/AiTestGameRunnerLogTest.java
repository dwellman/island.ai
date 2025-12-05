package com.demo.island.sim;

import com.demo.island.game.GameSession;
import com.demo.island.world.IslandTile;
import com.demo.island.world.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiTestGameRunnerLogTest {

    @Test
    void formatTurnLogPrefixesTimeAndCoords() {
        GameSession session = GameSession.newSession();
        String msg = "PlayerToolEngine: LOOK mood=CURIOUS";

        String formatted = AiTestGameRunner.formatTurnLog(session, null, msg);

        Position pos = session.getMap().get(session.getLocation().getTileId())
                .map(IslandTile::getPosition)
                .orElse(new Position(0, 0));
        String expectedPrefix = session.getClock().formatRemainingBracketed()
                + " (" + pos.x() + ", " + pos.y() + ") -- ";
        assertThat(formatted).startsWith(expectedPrefix);
        assertThat(formatted).endsWith(msg);
    }
}
