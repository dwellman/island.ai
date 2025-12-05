package com.demo.island.world;

import org.junit.jupiter.api.Test;

import com.demo.island.world.Direction8;

import static org.assertj.core.api.Assertions.assertThat;

class CaveLayoutTest {

    @Test
    void caveHasFourPlotsInALine() {
        IslandMap map = IslandGardener.garden();

        IslandTile mouth = map.get("T_CAVE_ENTRANCE").orElseThrow();
        IslandTile first = map.get("T_CAVE_FIRST_CHAMBER").orElseThrow();
        IslandTile deep = map.get("T_CAVE_DEEP_CHAMBER").orElseThrow();
        IslandTile back = map.get("T_CAVE_BACK_CHAMBER").orElseThrow();

        assertThat(first.getPosition()).isEqualTo(mouth.getPosition().step(Direction8.E));
        assertThat(deep.getPosition()).isEqualTo(first.getPosition().step(Direction8.E));
        assertThat(back.getPosition()).isEqualTo(deep.getPosition().step(Direction8.E));

        assertThat(mouth.getContext().getBaseDescription()).contains("drag half the ship in here");
        assertThat(first.getContext().getBaseDescription()).contains("old torch").contains("kerosene");
        assertThat(deep.getContext().getBaseDescription()).contains("tally marks").contains("rough map");
        assertThat(back.getContext().getBaseDescription()).contains("webs").contains("spiders");
    }
}
