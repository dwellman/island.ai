package com.demo.island.game;

import com.demo.island.world.PlayerLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class MonkeyPooEventTest {

    @BeforeEach
    void resetClock() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(new DiceService()));
    }

    @AfterEach
    void resetResolver() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(new DiceService()));
    }

    @Test
    void monkeyPooSuccessDodgedNoExtraTime() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(fixedRoll(18)));
        GameSession session = GameSession.newSession();
        session.setLocation(new PlayerLocation("T_VINE_FOREST"));

        GameActionResult result = GameEngine.perform(session, GameAction.simple(GameActionType.SEARCH));

        assertThat(result.isSuccess()).isTrue();
        // search cost 3 pips, no extra penalty on success
        assertThat(session.getClock().getTotalPips()).isEqualTo(3);
        assertThat(result.getMessage()).contains("foul").contains("splats");
    }

    @Test
    void monkeyPooFailureAddsPenaltyAndNarration() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(fixedRoll(1)));
        GameSession session = GameSession.newSession();
        session.setLocation(new PlayerLocation("T_VINE_FOREST"));

        GameActionResult result = GameEngine.perform(session, GameAction.simple(GameActionType.SEARCH));

        assertThat(result.isSuccess()).isTrue();
        // search cost 3 + penalty 5 (fail + nat1)
        assertThat(session.getClock().getTotalPips()).isEqualTo(8);
        assertThat(result.getMessage()).contains("monkeys").contains("wet smack");
    }

    @Test
    void outsideMonkeyTerritoryNoEvent() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(fixedRoll(1)));
        GameSession session = GameSession.newSession();
        session.setLocation(PlayerLocation.spawn());

        GameActionResult result = GameEngine.perform(session, GameAction.simple(GameActionType.SEARCH));

        assertThat(session.getClock().getTotalPips()).isEqualTo(3);
        assertThat(result.getMessage().toLowerCase()).doesNotContain("monkey");
    }

    private static DiceService fixedRoll(int value) {
        return new DiceService(new Random() {
            @Override
            public int nextInt(int bound) {
                return Math.max(0, Math.min(bound - 1, value - 1));
            }
        });
    }
}
