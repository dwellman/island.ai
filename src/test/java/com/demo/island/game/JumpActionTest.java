package com.demo.island.game;

import com.demo.island.world.Direction8;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class JumpActionTest {

    @AfterEach
    void resetResolver() {
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(new DiceService()));
    }

    @Test
    void jumpSuccessMovesAndCostsTime() {
        // Force high roll (18)
        DiceService dice = new DiceService(new Random() {
            @Override
            public int nextInt(int bound) {
                return 17;
            }
        });
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(dice));

        GameSession session = GameSession.newSession();
        GameAction action = GameAction.move(GameActionType.JUMP, Direction8.N);
        GameActionResult res = GameEngine.perform(session, action);

        assertThat(res.isSuccess()).isTrue();
        assertThat(session.getLocation().getTileId()).isEqualTo("T_CAMP");
        // Camp difficulty NORMAL -> walk cost 5
        assertThat(session.getClock().getTotalPips()).isEqualTo(5);
    }

    @Test
    void jumpFailureKeepsPlayerAndStillCostsTime() {
        // Force natural 1
        DiceService dice = new DiceService(new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });
        GameEngine.setChallengeResolverForTests(new ChallengeResolver(dice));

        GameSession session = GameSession.newSession();
        GameAction action = GameAction.move(GameActionType.JUMP, Direction8.N);
        GameActionResult res = GameEngine.perform(session, action);

        assertThat(res.isSuccess()).isFalse();
        assertThat(session.getLocation().getTileId()).isEqualTo("T_WRECK_BEACH");
        // walk cost 5 +2 failure +2 nat1 = 9
        assertThat(session.getClock().getTotalPips()).isEqualTo(9);
    }

    @Test
    void jumpNowhereDoesNotMoveOrCostTime() {
        GameSession session = GameSession.newSession();
        GameAction action = GameAction.move(GameActionType.JUMP, Direction8.S);
        GameActionResult res = GameEngine.perform(session, action);

        assertThat(res.isSuccess()).isFalse();
        assertThat(session.getLocation().getTileId()).isEqualTo("T_WRECK_BEACH");
        assertThat(session.getClock().getTotalPips()).isZero();
    }
}
