package com.demo.island.world;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the linear cave corridor the player can explore.
 */
final class CaveLayouts {

    private static final Position MOUTH_POS = new Position(2, 2);
    private static final Position FIRST_CHAMBER_POS = new Position(3, 2);
    private static final Position DEEP_CHAMBER_POS = new Position(4, 2);
    private static final Position BACK_CHAMBER_POS = new Position(5, 2);

    private CaveLayouts() {
    }

    static void apply(IslandMap map) {
        map.put(mouth());
        map.put(firstChamber());
        map.put(deepChamber());
        map.put(backChamber());
    }

    private static IslandTile mouth() {
        String desc = """
                The cave mouth slopes down from the daylight. Someone tried to drag half the ship in here: a broken crate, a twisted plank, a bent metal frame. They left the mess when they realised stone walls would not make this a home.
                """.trim();
        TileContext ctx = new TileContext(desc);
        return new IslandTile(
                "T_CAVE_ENTRANCE",
                TileKind.ANCHOR,
                MOUTH_POS,
                "cave",
                "interior",
                "mid",
                TerrainDifficulty.NORMAL,
                TileSafety.NORMAL,
                true,
                features(EnumSet.of(TerrainFeature.CAVE_MOUTH, TerrainFeature.ROCK_WALL)),
                PlantFamily.LICHEN,
                List.of(PlantFamily.FUNGUS),
                PlantDensity.SPARSE,
                ctx
        );
    }

    private static IslandTile firstChamber() {
        String desc = """
                The first chamber dips wider. Light from the entrance still reaches the sand and rock. An old torch lies in the sand, a stick wrapped in burned fabric; only a thin band of cloth is left unburned. A dented metal kerosene can sits on a low ledge, something sloshing inside with a sharp, oily smell. Pale stone chips litter the ground nearby where flint was struck. Someone lived here briefly, then chose to move on.
                """.trim();
        TileContext ctx = new TileContext(desc);
        return new IslandTile(
                "T_CAVE_FIRST_CHAMBER",
                TileKind.GARDENED,
                FIRST_CHAMBER_POS,
                "cave",
                "interior",
                "mid",
                TerrainDifficulty.NORMAL,
                TileSafety.NORMAL,
                true,
                features(EnumSet.of(TerrainFeature.ROCK_WALL)),
                PlantFamily.FUNGUS,
                List.of(PlantFamily.LICHEN),
                PlantDensity.SPARSE,
                ctx
        );
    }

    private static IslandTile deepChamber() {
        String desc = """
                The deep chamber is almost dark. Without a torch the walls are only shapes and thin grey light. In torchlight the carvings sharpen into a rough map of the island with marks for the shipwreck, your camp, and this cave. Beside it, groups of tally marks stack up in fives, counting long nights before the castaway built the treehouse elsewhere.
                """.trim();
        TileContext ctx = new TileContext(desc);
        return new IslandTile(
                "T_CAVE_DEEP_CHAMBER",
                TileKind.GARDENED,
                DEEP_CHAMBER_POS,
                "cave",
                "interior",
                "mid",
                TerrainDifficulty.HARD,
                TileSafety.NORMAL,
                true,
                features(EnumSet.of(TerrainFeature.ROCK_WALL)),
                PlantFamily.FUNGUS,
                List.of(PlantFamily.LICHEN),
                PlantDensity.SPARSE,
                ctx
        );
    }

    private static IslandTile backChamber() {
        String desc = """
                The back chamber is black without a light source. Ahead the dark is full of faint dry rustling. With any light you can see thick webs blocking the back wall and spiders clinging to the strands, still and watching. No marks, no writing—just a boundary and the sound of legs on stone.
                """.trim();
        TileContext ctx = new TileContext(desc);
        return new IslandTile(
                "T_CAVE_BACK_CHAMBER",
                TileKind.GARDENED,
                BACK_CHAMBER_POS,
                "cave",
                "interior",
                "mid",
                TerrainDifficulty.EXTREME,
                TileSafety.NORMAL,
                true,
                features(EnumSet.of(TerrainFeature.ROCK_WALL)),
                PlantFamily.FUNGUS,
                List.of(PlantFamily.LICHEN),
                PlantDensity.SPARSE,
                ctx
        );
    }

    private static Set<TerrainFeature> features(Set<TerrainFeature> base) {
        return java.util.Collections.unmodifiableSet(base);
    }
}
