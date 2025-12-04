package com.demo.island.world;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GardenCoverageTest {

    @Test
    void coversAllIslandEligiblePositions() {
        IslandMap map = IslandGardener.garden();
        int expectedCount = (WorldGeometry.ISLAND_MAX_X - WorldGeometry.ISLAND_MIN_X + 1)
                * (WorldGeometry.ISLAND_MAX_Y - WorldGeometry.ISLAND_MIN_Y + 1);
        assertThat(map.allTiles()).hasSize(expectedCount);

        // Anchors preserved
        for (AnchorTile anchor : AnchorTiles.all()) {
            IslandTile tile = map.get(anchor.getPosition()).orElseThrow();
            assertThat(tile.getTileId()).isEqualTo(anchor.getTileId());
            assertThat(tile.getKind()).isEqualTo(TileKind.ANCHOR);
            assertThat(tile.getBiome()).isEqualTo(anchor.getBiome());
            assertThat(tile.getRegion()).isEqualTo(anchor.getRegion());
            assertThat(tile.getElevation()).isEqualTo(anchor.getElevation());
            assertThat(tile.getContext().getBaseDescription()).isNotEmpty();
        }
    }

    @Test
    void connectivityFromSpawn() {
        IslandMap map = IslandGardener.garden();
        Position spawn = AnchorTiles.startTile().getPosition();
        Set<Position> visited = new HashSet<>();
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
        queue.add(spawn);
        visited.add(spawn);

        while (!queue.isEmpty()) {
            Position p = queue.poll();
            for (Direction8 dir : Direction8.values()) {
                Position next = p.step(dir);
                map.get(next).filter(IslandTile::isWalkable).ifPresent(tile -> {
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                });
            }
        }

        // All walkable tiles reachable
        long walkableCount = map.allTiles().stream().filter(IslandTile::isWalkable).count();
        assertThat(visited).hasSize((int) walkableCount);

        // All anchors reachable
        for (AnchorTile anchor : AnchorTiles.all()) {
            assertThat(visited).contains(anchor.getPosition());
        }
    }

    @Test
    void tileContextBasics() {
        IslandMap map = IslandGardener.garden();
        IslandTile sample = map.allTiles().iterator().next();
        TileContext ctx = sample.getContext();
        assertThat(ctx.getBaseDescription()).isNotEmpty();
        assertThat(ctx.getCurrentDescription()).isEqualTo(ctx.getBaseDescription());
        ctx.setCurrentDescription("New desc");
        ctx.appendHistory(1, TurnClock.TimePhase.LIGHT, "TEST", "Changed desc");
        assertThat(ctx.getCurrentDescription()).isEqualTo("New desc");
        assertThat(ctx.getHistory()).hasSize(1);
        assertThat(ctx.getHistory().get(0).getSequenceNumber()).isEqualTo(1);
    }
}
