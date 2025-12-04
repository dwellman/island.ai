package com.demo.island;

import com.demo.island.core.WorldState;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.TurnEngine;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies canonical topology and movement: Camp <-> Bamboo (N/S), Camp <-> Vines (E/W).
 */
public class JoystickWalkabout01Test {

    @Test
    void campBambooVinesLoop() {
        WorldState world = WorldFactory.createDemoWorld("walkabout");
        TurnEngine engine = new TurnEngine();
        DmAgent dm = new SimpleDmStubAgent();

        // Step 0: start at camp
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_CAMP");

        // Step 1: LOOK AROUND (no move)
        engine.runTurn(world, new PlayerCommand("player-1", "LOOK AROUND"), dm);
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_CAMP");

        // Step 2: GO N -> Bamboo
        engine.runTurn(world, new PlayerCommand("player-1", "GO N"), dm);
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_BAMBOO");

        // Step 3: GO S -> Camp
        engine.runTurn(world, new PlayerCommand("player-1", "GO S"), dm);
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_CAMP");

        // Step 4: GO E -> Vines
        engine.runTurn(world, new PlayerCommand("player-1", "GO E"), dm);
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_VINES");

        // Step 5: GO W -> Camp
        engine.runTurn(world, new PlayerCommand("player-1", "GO W"), dm);
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_CAMP");
    }
}
