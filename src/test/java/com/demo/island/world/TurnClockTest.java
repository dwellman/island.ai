package com.demo.island.world;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnClockTest {

    @Test
    void initialState() {
        TurnClock clock = new TurnClock();
        assertThat(clock.getTurnIndex()).isZero();
        assertThat(clock.getPhase()).isEqualTo(TurnClock.TimePhase.LIGHT);
        assertThat(clock.isOutOfTime()).isFalse();
    }

    @Test
    void phaseMapping() {
        assertThat(TurnClock.phaseFor(0)).isEqualTo(TurnClock.TimePhase.LIGHT);
        assertThat(TurnClock.phaseFor(5)).isEqualTo(TurnClock.TimePhase.LIGHT);
        assertThat(TurnClock.phaseFor(11)).isEqualTo(TurnClock.TimePhase.LIGHT);
        assertThat(TurnClock.phaseFor(12)).isEqualTo(TurnClock.TimePhase.DUSK);
        assertThat(TurnClock.phaseFor(18)).isEqualTo(TurnClock.TimePhase.DUSK);
        assertThat(TurnClock.phaseFor(23)).isEqualTo(TurnClock.TimePhase.DUSK);
        assertThat(TurnClock.phaseFor(24)).isEqualTo(TurnClock.TimePhase.DARK);
        assertThat(TurnClock.phaseFor(30)).isEqualTo(TurnClock.TimePhase.DARK);
        assertThat(TurnClock.phaseFor(35)).isEqualTo(TurnClock.TimePhase.DARK);
        assertThat(TurnClock.phaseFor(36)).isEqualTo(TurnClock.TimePhase.DARK);
    }

    @Test
    void turnLimitFlag() {
        TurnClock clock = new TurnClock();
        for (int i = 0; i < 36; i++) {
            assertThat(clock.isOutOfTime()).isFalse();
            clock.applyMoveOutcome(MoveOutcome.MOVE_OK, 1);
        }
        assertThat(clock.getTurnIndex()).isEqualTo(36);
        assertThat(clock.isOutOfTime()).isTrue();
        assertThat(clock.getPhase()).isEqualTo(TurnClock.TimePhase.DARK);
    }

    @Test
    void blockedMovesDoNotAdvance() {
        TurnClock clock = new TurnClock();
        clock.applyMoveOutcome(MoveOutcome.MOVE_BLOCKED_NO_TILE, 0);
        clock.applyMoveOutcome(MoveOutcome.MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY, 0);
        assertThat(clock.getTurnIndex()).isZero();
        assertThat(clock.getPhase()).isEqualTo(TurnClock.TimePhase.LIGHT);
        assertThat(clock.isOutOfTime()).isFalse();
    }

    @Test
    void mixedSequence() {
        TurnClock clock = new TurnClock();
        // blocked first
        clock.applyMoveOutcome(MoveOutcome.MOVE_BLOCKED_NO_TILE, 0);
        assertThat(clock.getTurnIndex()).isZero();
        // move ok to camp
        clock.applyMoveOutcome(MoveOutcome.MOVE_OK, 1);
        assertThat(clock.getTurnIndex()).isEqualTo(1);
        // move ok back
        clock.applyMoveOutcome(MoveOutcome.MOVE_OK, 1);
        assertThat(clock.getTurnIndex()).isEqualTo(2);
    }
}
