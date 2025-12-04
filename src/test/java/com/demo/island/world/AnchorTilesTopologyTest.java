package com.demo.island.world;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorTilesTopologyTest {

    @Test
    void manifestHasAllAnchorsWithUniquePositions() {
        List<AnchorTile> all = AnchorTiles.all();
        assertThat(all).hasSize(12);
        assertThat(AnchorTiles.ids()).containsExactlyInAnyOrder(
                "T_WRECK_BEACH", "T_TIDEPOOL_ROCKS", "T_HIDDEN_COVE", "T_CAMP", "T_STREAM",
                "T_VINE_FOREST", "T_BAMBOO_GROVE", "T_WATERFALL_POOL", "T_CAVE_ENTRANCE",
                "T_CLIFF_EDGE", "T_OLD_RUINS", "T_SIGNAL_HILL"
        );

        Set<Position> positions = all.stream().map(AnchorTile::getPosition).collect(Collectors.toSet());
        assertThat(positions).hasSize(all.size());

        all.forEach(tile -> assertThat(WorldGeometry.classify(tile.getPosition()))
                .as("Tile %s should be island-eligible", tile.getTileId())
                .isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE));
    }

    @Test
    void startAndExitFlags() {
        List<AnchorTile> starts = AnchorTiles.all().stream().filter(AnchorTile::isStartTile).toList();
        assertThat(starts).hasSize(1);
        assertThat(starts.get(0).getTileId()).isEqualTo("T_WRECK_BEACH");
        assertThat(starts.get(0).getPosition()).isEqualTo(new Position(0, 0));

        List<AnchorTile> exits = AnchorTiles.all().stream().filter(AnchorTile::isExitCandidate).toList();
        assertThat(exits).hasSize(1);
        assertThat(exits.get(0).getTileId()).isEqualTo("T_SIGNAL_HILL");
    }

    @Test
    void neighborMappingMatchesSpec() {
        Map<String, Map<Direction8, String>> expected = Map.ofEntries(
                Map.entry("T_WRECK_BEACH", Map.of(
                        Direction8.N, "T_CAMP",
                        Direction8.NE, "T_STREAM",
                        Direction8.E, "T_TIDEPOOL_ROCKS",
                        Direction8.W, "T_HIDDEN_COVE",
                        Direction8.NW, "T_VINE_FOREST"
                )),
                Map.entry("T_TIDEPOOL_ROCKS", Map.of(
                        Direction8.N, "T_STREAM",
                        Direction8.W, "T_WRECK_BEACH",
                        Direction8.NW, "T_CAMP"
                )),
                Map.entry("T_HIDDEN_COVE", Map.of(
                        Direction8.N, "T_VINE_FOREST",
                        Direction8.NE, "T_CAMP",
                        Direction8.E, "T_WRECK_BEACH"
                )),
                Map.entry("T_CAMP", Map.of(
                        Direction8.N, "T_BAMBOO_GROVE",
                        Direction8.NE, "T_WATERFALL_POOL",
                        Direction8.E, "T_STREAM",
                        Direction8.SE, "T_TIDEPOOL_ROCKS",
                        Direction8.S, "T_WRECK_BEACH",
                        Direction8.SW, "T_HIDDEN_COVE",
                        Direction8.W, "T_VINE_FOREST",
                        Direction8.NW, "T_OLD_RUINS"
                )),
                Map.entry("T_STREAM", Map.of(
                        Direction8.N, "T_WATERFALL_POOL",
                        Direction8.NE, "T_CAVE_ENTRANCE",
                        Direction8.S, "T_TIDEPOOL_ROCKS",
                        Direction8.SW, "T_WRECK_BEACH",
                        Direction8.W, "T_CAMP",
                        Direction8.NW, "T_BAMBOO_GROVE"
                )),
                Map.entry("T_VINE_FOREST", Map.of(
                        Direction8.N, "T_OLD_RUINS",
                        Direction8.NE, "T_BAMBOO_GROVE",
                        Direction8.E, "T_CAMP",
                        Direction8.SE, "T_WRECK_BEACH",
                        Direction8.S, "T_HIDDEN_COVE"
                )),
                Map.entry("T_BAMBOO_GROVE", Map.of(
                        Direction8.N, "T_CLIFF_EDGE",
                        Direction8.E, "T_WATERFALL_POOL",
                        Direction8.SE, "T_STREAM",
                        Direction8.S, "T_CAMP",
                        Direction8.SW, "T_VINE_FOREST",
                        Direction8.W, "T_OLD_RUINS"
                )),
                Map.entry("T_WATERFALL_POOL", Map.of(
                        Direction8.E, "T_CAVE_ENTRANCE",
                        Direction8.S, "T_STREAM",
                        Direction8.SW, "T_CAMP",
                        Direction8.W, "T_BAMBOO_GROVE",
                        Direction8.NW, "T_CLIFF_EDGE"
                )),
                Map.entry("T_CAVE_ENTRANCE", Map.of(
                        Direction8.SW, "T_STREAM",
                        Direction8.W, "T_WATERFALL_POOL"
                )),
                Map.entry("T_CLIFF_EDGE", Map.of(
                        Direction8.N, "T_SIGNAL_HILL",
                        Direction8.SE, "T_WATERFALL_POOL",
                        Direction8.S, "T_BAMBOO_GROVE",
                        Direction8.SW, "T_OLD_RUINS"
                )),
                Map.entry("T_OLD_RUINS", Map.of(
                        Direction8.NE, "T_CLIFF_EDGE",
                        Direction8.E, "T_BAMBOO_GROVE",
                        Direction8.SE, "T_CAMP",
                        Direction8.S, "T_VINE_FOREST"
                )),
                Map.entry("T_SIGNAL_HILL", Map.of(
                        Direction8.S, "T_CLIFF_EDGE"
                ))
        );

        expected.forEach((id, neighborMap) -> {
            AnchorTile tile = AnchorTiles.byId(id).orElseThrow();
            neighborMap.forEach((dir, expectedNeighbor) ->
                    assertThat(tile.getNeighbor(dir))
                            .as("Neighbor %s %s", id, dir)
                            .isEqualTo(expectedNeighbor));
        });
    }

    @Test
    void neighborPositionsMatchDirectionDeltas() {
        for (AnchorTile tile : AnchorTiles.all()) {
            Position origin = tile.getPosition();
            tile.getNeighbors().forEach((dir, neighborId) -> {
                AnchorTile neighbor = AnchorTiles.byId(neighborId).orElseThrow();
                Position expectedPos = origin.step(dir);
                assertThat(neighbor.getPosition())
                        .as("Position of %s should be origin %s + %s", neighborId, origin, dir)
                        .isEqualTo(expectedPos);
            });
        }
    }

    @Test
    void reachabilityFromSpawn() {
        // Spawn tile is Wreck Beach at (0,0)
        assertThat(AnchorTiles.byPosition(new Position(0, 0)).map(AnchorTile::getTileId))
                .contains("T_WRECK_BEACH");

        // N -> Camp
        Position campPos = WorldGeometry.apply(new Position(0, 0), Direction8.N);
        assertThat(AnchorTiles.byPosition(campPos).map(AnchorTile::getTileId)).contains("T_CAMP");

        // N,N -> Bamboo Grove
        Position bambooPos = WorldGeometry.apply(campPos, Direction8.N);
        assertThat(AnchorTiles.byPosition(bambooPos).map(AnchorTile::getTileId)).contains("T_BAMBOO_GROVE");

        // N,N,N,N -> Signal Hill
        Position hillPos = bambooPos;
        hillPos = WorldGeometry.apply(hillPos, Direction8.N);
        hillPos = WorldGeometry.apply(hillPos, Direction8.N);
        assertThat(AnchorTiles.byPosition(hillPos).map(AnchorTile::getTileId)).contains("T_SIGNAL_HILL");

        // NE, NE from spawn -> Cave Entrance
        Position p = WorldGeometry.apply(new Position(0, 0), Direction8.NE);
        p = WorldGeometry.apply(p, Direction8.NE);
        assertThat(AnchorTiles.byPosition(p).map(AnchorTile::getTileId)).contains("T_CAVE_ENTRANCE");
    }
}
