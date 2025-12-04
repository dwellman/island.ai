package com.demo.island.engine.check;

import com.demo.island.core.GameSession;
import com.demo.island.core.Player;
import com.demo.island.core.TextFace;
import com.demo.island.core.WorldState;
import com.demo.island.engine.dice.DiceService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckServiceTest {

    @Test
    void perceptionUsesAwarenessStat() {
        WorldState worldState = new WorldState(new GameSession("s", 10));
        Player player = new Player("p1", "Test", "Navigator", "T", TextFace.empty("test"));
        player.getStats().set("AWR", 10);
        worldState.getPlayers().put(player.getPlayerId(), player);

        CheckService service = new CheckService(new DiceService(1L));
        CheckRequest request = new CheckRequest(CheckType.PERCEPTION, CheckSubjectKind.PLAYER, "p1", 5);
        CheckResult result = service.evaluate(worldState, request);

        assertThat(result.getRequest().getType()).isEqualTo(CheckType.PERCEPTION);
        assertThat(result.getModifier()).isEqualTo(10);
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(result.getModifier()); // roll >= 1
        assertThat(result.isSuccess()).isTrue(); // modifier alone beats DC
    }

    @Test
    void hearingGetsNightBonus() {
        WorldState worldState = new WorldState(new GameSession("s", 10));
        worldState.getSession().setTimePhase(GameSession.TimePhase.DARK);
        Player player = new Player("p1", "Test", "Navigator", "T", TextFace.empty("test"));
        player.getStats().set("AWR", 0);
        worldState.getPlayers().put(player.getPlayerId(), player);

        CheckService service = new CheckService(new DiceService(2L));
        CheckRequest request = new CheckRequest(CheckType.HEARING, CheckSubjectKind.PLAYER, "p1", 15);
        CheckResult result = service.evaluate(worldState, request);

        assertThat(result.getModifier()).isEqualTo(2); // night bonus applied
        assertThat(result.getTotal()).isEqualTo(result.getRoll() + 2);
    }
}
