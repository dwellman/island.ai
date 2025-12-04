package com.demo.island.engine;

import com.demo.island.core.WorldState;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnEngineAdvanceTest {

    @Test
    void lookAroundConsumesTurn() {
        WorldState world = WorldFactory.createDemoWorld("test-session");
        TurnEngine engine = new TurnEngine();
        DmAgent stub = input -> {
            DmDecision decision = new DmDecision("You look around.", true);
            return decision;
        };

        assertThat(world.getSession().getTurnNumber()).isZero();

        engine.runTurn(world, new PlayerCommand("player-1", "LOOK AROUND"), stub);

        assertThat(world.getSession().getTurnNumber()).isEqualTo(1);
    }

    @Test
    void bareJoystickVerbsDoNotConsumeTurn() {
        String[] verbs = {"LOOK", "GO", "TAKE", "DROP", "JUMP", "SWIM", "CLIMB"};
        for (String verb : verbs) {
            WorldState world = WorldFactory.createDemoWorld("test-session-" + verb);
            TurnEngine engine = new TurnEngine();
            DmAgent clarifier = input -> new DmDecision("Clarify", true);
            assertThat(world.getSession().getTurnNumber()).isZero();
            engine.runTurn(world, new PlayerCommand("player-1", verb), clarifier);
            assertThat(world.getSession().getTurnNumber()).isEqualTo(1);
        }
    }

    @Test
    void joystickWithArgsConsumesTurn() {
        WorldState world = WorldFactory.createDemoWorld("test-session");
        TurnEngine engine = new TurnEngine();
        DmAgent consume = input -> new DmDecision("You act.", true);

        engine.runTurn(world, new PlayerCommand("player-1", "GO N"), consume);
        assertThat(world.getSession().getTurnNumber()).isEqualTo(1);
    }

    @Test
    void metaCommandsDoNotConsumeTurn() {
        String[] meta = {"HELP", "TIME", ""};
        for (String cmd : meta) {
            WorldState world = WorldFactory.createDemoWorld("meta-" + cmd);
            TurnEngine engine = new TurnEngine();
            DmAgent metaDm = input -> new DmDecision("Meta", false);
            assertThat(world.getSession().getTurnNumber()).isZero();
            engine.runTurn(world, new PlayerCommand("player-1", cmd), metaDm);
            assertThat(world.getSession().getTurnNumber()).isZero();
        }
    }
}
