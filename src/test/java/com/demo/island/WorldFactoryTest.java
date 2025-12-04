package com.demo.island;

import com.demo.island.core.GameSession;
import com.demo.island.core.ItemInstance;
import com.demo.island.core.WorldState;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WorldFactoryTest {

    @Test
    void demoWorldSeedsTilesItemsAndPlayer() {
        WorldState worldState = WorldFactory.createDemoWorld("test-session");

        assertThat(worldState.getTiles()).containsKeys("T_CAMP", "T_BAMBOO", "T_VINES");
        assertThat(worldState.getPlayers()).containsKey("player-1");
        assertThat(worldState.getCreatures()).containsKeys("ghost", "monkeys");
        assertThat(worldState.getSession().getTimePhase()).isEqualTo(GameSession.TimePhase.LIGHT);

        ItemInstance skeleton = worldState.getItems().values().stream()
                .filter(item -> item.getItemTypeId().equals("skeleton_body"))
                .findFirst()
                .orElseThrow();
        ItemInstance machete = worldState.getItems().values().stream()
                .filter(item -> item.getItemTypeId().equals("machete"))
                .findFirst()
                .orElseThrow();
        ItemInstance vinesBundle = worldState.getItems().values().stream()
                .filter(item -> item.getItemTypeId().equals("vine_bundle"))
                .findFirst()
                .orElseThrow();

        assertThat(skeleton.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.TILE);
        assertThat(machete.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.ITEM);
        assertThat(machete.getContainedByItemId()).isEqualTo(skeleton.getItemId());
        assertThat(vinesBundle.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.TILE);
        assertThat(vinesBundle.getOwnerId()).isEqualTo("T_VINES");

        // Canonical neighbors
        assertThat(worldState.getTile("T_CAMP").getNeighbor(com.demo.island.core.Tile.Direction.N)).isEqualTo("T_BAMBOO");
        assertThat(worldState.getTile("T_CAMP").getNeighbor(com.demo.island.core.Tile.Direction.E)).isEqualTo("T_VINES");
        assertThat(worldState.getTile("T_BAMBOO").getNeighbor(com.demo.island.core.Tile.Direction.S)).isEqualTo("T_CAMP");
        assertThat(worldState.getTile("T_VINES").getNeighbor(com.demo.island.core.Tile.Direction.W)).isEqualTo("T_CAMP");
    }
}
