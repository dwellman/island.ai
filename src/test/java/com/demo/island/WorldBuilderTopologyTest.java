package com.demo.island;

import com.demo.island.core.GameSession;
import com.demo.island.core.Tile;
import com.demo.island.core.WorldState;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WORLD-BUILDER-TOPOLOGY-01
 * Verifies the canonical island topology, biomes/regions, neighbors, and starting session state.
 */
public class WorldBuilderTopologyTest {

    @Test
    void canonicalTopologyAndStartState() {
        WorldState world = WorldFactory.createDemoWorld("topology");

        // Tiles present
        assertThat(world.getTiles()).hasSize(3)
                .containsKeys("T_CAMP", "T_BAMBOO", "T_VINES");

        // Biomes and regions
        assertThat(world.getTile("T_CAMP").getBiome()).isEqualTo("beach");
        assertThat(world.getTile("T_CAMP").getRegion()).isEqualTo("coast");

        assertThat(world.getTile("T_BAMBOO").getBiome()).isEqualTo("bamboo_forest");
        assertThat(world.getTile("T_BAMBOO").getRegion()).isEqualTo("interior");

        assertThat(world.getTile("T_VINES").getBiome()).isEqualTo("vine_forest");
        assertThat(world.getTile("T_VINES").getRegion()).isEqualTo("interior");

        // Neighbors
        assertThat(world.getTile("T_CAMP").getNeighbor(Tile.Direction.N)).isEqualTo("T_BAMBOO");
        assertThat(world.getTile("T_CAMP").getNeighbor(Tile.Direction.E)).isEqualTo("T_VINES");
        assertThat(world.getTile("T_CAMP").getNeighbor(Tile.Direction.S)).isNull();
        assertThat(world.getTile("T_CAMP").getNeighbor(Tile.Direction.W)).isNull();

        assertThat(world.getTile("T_BAMBOO").getNeighbor(Tile.Direction.S)).isEqualTo("T_CAMP");
        assertThat(world.getTile("T_BAMBOO").getNeighbor(Tile.Direction.N)).isNull();
        assertThat(world.getTile("T_BAMBOO").getNeighbor(Tile.Direction.E)).isNull();
        assertThat(world.getTile("T_BAMBOO").getNeighbor(Tile.Direction.W)).isNull();

        assertThat(world.getTile("T_VINES").getNeighbor(Tile.Direction.W)).isEqualTo("T_CAMP");
        assertThat(world.getTile("T_VINES").getNeighbor(Tile.Direction.N)).isNull();
        assertThat(world.getTile("T_VINES").getNeighbor(Tile.Direction.E)).isNull();
        assertThat(world.getTile("T_VINES").getNeighbor(Tile.Direction.S)).isNull();

        // Start state
        assertThat(world.getPlayer("player-1").getCurrentTileId()).isEqualTo("T_CAMP");
        assertThat(world.getSession().getTurnNumber()).isZero();
        assertThat(world.getSession().getTimePhase()).isEqualTo(GameSession.TimePhase.LIGHT);
    }
}
