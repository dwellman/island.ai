package com.demo.island.world;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manifest of canonical anchor tiles and their topology.
 */
public final class AnchorTiles {

    private static final List<AnchorTile> MANIFEST = List.of(
            new AnchorTile(
                    "T_WRECK_BEACH",
                    "Wreck Beach",
                    "A stretch of wreckage-strewn beach where broken timbers and torn canvas lie in the sand.",
                    new Position(0, 0),
                    "beach",
                    "shoreline",
                    "low",
                    true,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_CAMP"),
                            Map.entry(Direction8.NE, "T_STREAM"),
                            Map.entry(Direction8.E, "T_TIDEPOOL_ROCKS"),
                            Map.entry(Direction8.W, "T_HIDDEN_COVE"),
                            Map.entry(Direction8.NW, "T_VINE_FOREST")
                    ))
            ),
            new AnchorTile(
                    "T_TIDEPOOL_ROCKS",
                    "Tidepool Rocks",
                    "Slippery rocks pocketed with tidepools; sharp edges and shifting surf make this spot hazardous.",
                    new Position(1, 0),
                    "rocky_shore",
                    "shoreline",
                    "low",
                    false,
                    false,
                    true,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_STREAM"),
                            Map.entry(Direction8.W, "T_WRECK_BEACH"),
                            Map.entry(Direction8.NW, "T_CAMP")
                    ))
            ),
            new AnchorTile(
                    "T_HIDDEN_COVE",
                    "Hidden Cove",
                    "A sheltered cove tucked away from the main beach, quiet water lapping against stone walls.",
                    new Position(-1, 0),
                    "cove",
                    "shoreline",
                    "low",
                    false,
                    false,
                    false,
                    true,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_VINE_FOREST"),
                            Map.entry(Direction8.NE, "T_CAMP"),
                            Map.entry(Direction8.E, "T_WRECK_BEACH")
                    ))
            ),
            new AnchorTile(
                    "T_CAMP",
                    "Camp",
                    "A small clearing just north of the beach, a natural camp where the forest opens. The beach lies to the south; deeper trees and higher ground rise to the north, with water off to the east and thicker vines to the west.",
                    new Position(0, 1),
                    "light_forest",
                    "interior_edge",
                    "low",
                    false,
                    false,
                    false,
                    false,
                    true,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_BAMBOO_GROVE"),
                            Map.entry(Direction8.NE, "T_WATERFALL_POOL"),
                            Map.entry(Direction8.E, "T_STREAM"),
                            Map.entry(Direction8.SE, "T_TIDEPOOL_ROCKS"),
                            Map.entry(Direction8.S, "T_WRECK_BEACH"),
                            Map.entry(Direction8.SW, "T_HIDDEN_COVE"),
                            Map.entry(Direction8.W, "T_VINE_FOREST"),
                            Map.entry(Direction8.NW, "T_OLD_RUINS")
                    ))
            ),
            new AnchorTile(
                    "T_STREAM",
                    "Stream Bank",
                    "A narrow stream of fresh water cuts through the interior, its banks muddy but firm, a welcome source of water.",
                    new Position(1, 1),
                    "stream_bank",
                    "interior",
                    "low_mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_WATERFALL_POOL"),
                            Map.entry(Direction8.NE, "T_CAVE_ENTRANCE"),
                            Map.entry(Direction8.S, "T_TIDEPOOL_ROCKS"),
                            Map.entry(Direction8.SW, "T_WRECK_BEACH"),
                            Map.entry(Direction8.W, "T_CAMP"),
                            Map.entry(Direction8.NW, "T_BAMBOO_GROVE")
                    ))
            ),
            new AnchorTile(
                    "T_VINE_FOREST",
                    "Vine Forest",
                    "A tangle of hanging vines and low branches, the forest thick and draped in green cordage.",
                    new Position(-1, 1),
                    "vine_forest",
                    "interior",
                    "low_mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_OLD_RUINS"),
                            Map.entry(Direction8.NE, "T_BAMBOO_GROVE"),
                            Map.entry(Direction8.E, "T_CAMP"),
                            Map.entry(Direction8.SE, "T_WRECK_BEACH"),
                            Map.entry(Direction8.S, "T_HIDDEN_COVE")
                    ))
            ),
            new AnchorTile(
                    "T_BAMBOO_GROVE",
                    "Bamboo Grove",
                    "Stands of bamboo rise above the camp, creaking softly; heading north climbs toward higher cliffs.",
                    new Position(0, 2),
                    "bamboo_forest",
                    "interior",
                    "mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_CLIFF_EDGE"),
                            Map.entry(Direction8.E, "T_WATERFALL_POOL"),
                            Map.entry(Direction8.SE, "T_STREAM"),
                            Map.entry(Direction8.S, "T_CAMP"),
                            Map.entry(Direction8.SW, "T_VINE_FOREST"),
                            Map.entry(Direction8.W, "T_OLD_RUINS")
                    ))
            ),
            new AnchorTile(
                    "T_WATERFALL_POOL",
                    "Waterfall Pool",
                    "A clear pool fed by a thin waterfall. The stream runs below, and paths lead toward the cliffs above.",
                    new Position(1, 2),
                    "waterfall",
                    "interior",
                    "mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.E, "T_CAVE_ENTRANCE"),
                            Map.entry(Direction8.S, "T_STREAM"),
                            Map.entry(Direction8.SW, "T_CAMP"),
                            Map.entry(Direction8.W, "T_BAMBOO_GROVE"),
                            Map.entry(Direction8.NW, "T_CLIFF_EDGE")
                    ))
            ),
            new AnchorTile(
                    "T_CAVE_ENTRANCE",
                    "Cave Entrance",
                    "A dark opening in the rock, cold air drifting out from within the cave.",
                    new Position(2, 2),
                    "cave",
                    "interior",
                    "mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.SW, "T_STREAM"),
                            Map.entry(Direction8.W, "T_WATERFALL_POOL")
                    ))
            ),
            new AnchorTile(
                    "T_CLIFF_EDGE",
                    "Cliff Edge",
                    "The ground falls away here at a sheer cliff edge; below lie forest and streams, and above is the climb toward the hill.",
                    new Position(0, 3),
                    "cliff",
                    "high_ground",
                    "high",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.N, "T_SIGNAL_HILL"),
                            Map.entry(Direction8.SE, "T_WATERFALL_POOL"),
                            Map.entry(Direction8.S, "T_BAMBOO_GROVE"),
                            Map.entry(Direction8.SW, "T_OLD_RUINS")
                    ))
            ),
            new AnchorTile(
                    "T_OLD_RUINS",
                    "Old Ruins",
                    "Broken stone walls and toppled columns sit quietly off the main paths, relics of an older island story.",
                    new Position(-1, 2),
                    "ruins",
                    "interior",
                    "mid",
                    false,
                    false,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.NE, "T_CLIFF_EDGE"),
                            Map.entry(Direction8.E, "T_BAMBOO_GROVE"),
                            Map.entry(Direction8.SE, "T_CAMP"),
                            Map.entry(Direction8.S, "T_VINE_FOREST")
                    ))
            ),
            new AnchorTile(
                    "T_SIGNAL_HILL",
                    "Signal Hill",
                    "The high point of the island, a wind-swept hill with a wide view and a clear spot to signal for rescue.",
                    new Position(0, 4),
                    "high_point",
                    "high_ground",
                    "peak",
                    false,
                    true,
                    false,
                    false,
                    false,
                    new EnumMap<>(Map.ofEntries(
                            Map.entry(Direction8.S, "T_CLIFF_EDGE")
                    ))
            )
    );

    private static final Map<String, AnchorTile> BY_ID =
            MANIFEST.stream().collect(Collectors.toUnmodifiableMap(AnchorTile::getTileId, t -> t));
    private static final Map<Position, AnchorTile> BY_POS =
            MANIFEST.stream().collect(Collectors.toUnmodifiableMap(AnchorTile::getPosition, t -> t));
    private static final AnchorTile START_TILE = MANIFEST.stream()
            .filter(AnchorTile::isStartTile)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No start tile defined"));

    private AnchorTiles() {
    }

    public static List<AnchorTile> all() {
        return MANIFEST;
    }

    public static Optional<AnchorTile> byId(String tileId) {
        return Optional.ofNullable(BY_ID.get(tileId));
    }

    public static Optional<AnchorTile> byPosition(Position position) {
        return Optional.ofNullable(BY_POS.get(position));
    }

    public static String neighborOf(String tileId, Direction8 direction) {
        AnchorTile tile = BY_ID.get(tileId);
        if (tile == null) {
            return null;
        }
        return tile.getNeighbor(direction);
    }

    public static Set<String> ids() {
        return BY_ID.keySet();
    }

    public static AnchorTile startTile() {
        return START_TILE;
    }
}
