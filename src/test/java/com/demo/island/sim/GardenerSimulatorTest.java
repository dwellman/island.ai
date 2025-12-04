package com.demo.island.sim;

import com.demo.island.world.GameOverReason;
import com.demo.island.world.TurnClock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GardenerSimulatorTest {

    @Test
    void randomWalkIsDeterministicWithSeed() {
        GardenerRunConfig config = new GardenerRunConfig(15, 123L);
        GardenerRunResult first = GardenerSimulator.run(config);
        GardenerRunResult second = GardenerSimulator.run(config);

        assertThat(first.getStepsTaken()).isEqualTo(second.getStepsTaken());
        for (int i = 0; i < first.getSteps().size(); i++) {
            var a = first.getSteps().get(i);
            var b = second.getSteps().get(i);
            assertThat(a.getDirection()).isEqualTo(b.getDirection());
            assertThat(a.getMoveOutcome()).isEqualTo(b.getMoveOutcome());
            assertThat(a.getPlotId()).isEqualTo(b.getPlotId());
            assertThat(a.getTurnAfter()).isEqualTo(b.getTurnAfter());
            assertThat(a.getTimeCost()).isEqualTo(b.getTimeCost());
        }
        assertThat(first.getFinalPlotId()).isEqualTo(second.getFinalPlotId());
        assertThat(first.getFinalTurnIndex()).isEqualTo(second.getFinalTurnIndex());
        assertThat(first.getFinalPhase()).isEqualTo(second.getFinalPhase());
    }

    @Test
    void respectsMaxStepsAndTracksTimeCost() {
        GardenerRunConfig config = new GardenerRunConfig(10, 7L);
        GardenerRunResult result = GardenerSimulator.run(config);

        assertThat(result.getStepsTaken()).isLessThanOrEqualTo(10);
        int summedTime = result.getSteps().stream().mapToInt(GardenerStepLog::getTimeCost).sum();
        assertThat(result.getFinalTurnIndex()).isEqualTo(summedTime);
    }

    @Test
    void reachesOutOfTimeOnLongRun() {
        long[] seeds = {1L, 7L, 42L, 99L, 123L, 555L, 999L, 2024L};
        GardenerRunResult result = null;
        for (long seed : seeds) {
            GardenerRunConfig config = new GardenerRunConfig(200, seed);
            GardenerRunResult candidate = GardenerSimulator.run(config);
            if (candidate.isGameOver() && candidate.getGameOverReason() == GameOverReason.OUT_OF_TIME) {
                result = candidate;
                break;
            }
        }

        assertThat(result).withFailMessage("Expected at least one OUT_OF_TIME run among the seeds").isNotNull();
        assertThat(result.isGameOver()).isTrue();
        assertThat(result.getGameOverReason()).isEqualTo(GameOverReason.OUT_OF_TIME);
        assertThat(result.getFinalTurnIndex()).isGreaterThanOrEqualTo(TurnClock.TURN_LIMIT);
    }
}
