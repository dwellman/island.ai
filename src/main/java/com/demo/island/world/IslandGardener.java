package com.demo.island.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fills the island band with tiles, reusing anchors and generating gardened tiles.
 */
public final class IslandGardener {

    private IslandGardener() {
    }

    public static IslandMap garden() {
        IslandMap map = new IslandMap();

        // Place anchors
        for (AnchorTile anchor : AnchorTiles.all()) {
            TileContext ctx = new TileContext(anchor.getDescription());
            IslandTile tile = new IslandTile(
                    anchor.getTileId(),
                    TileKind.ANCHOR,
                    anchor.getPosition(),
                    anchor.getBiome(),
                    anchor.getRegion(),
                    anchor.getElevation(),
                    defaultDifficulty(anchor.getBiome()),
                    TileSafety.NORMAL,
                    true,
                    java.util.Set.of(),
                    floraPrimary(anchor.getBiome(), anchor.getRegion(), anchor.getElevation(), anchor.getPosition()),
                    floraSecondary(anchor.getBiome(), anchor.getRegion(), anchor.getElevation(), anchor.getPosition()),
                    floraDensity(anchor.getBiome(), anchor.getRegion(), anchor.getElevation(), anchor.getPosition()),
                    ctx
            );
            map.put(tile);
        }

        // Fill the island band
        for (int x = WorldGeometry.ISLAND_MIN_X; x <= WorldGeometry.ISLAND_MAX_X; x++) {
            for (int y = WorldGeometry.ISLAND_MIN_Y; y <= WorldGeometry.ISLAND_MAX_Y; y++) {
                Position pos = new Position(x, y);
                if (map.get(pos).isPresent()) {
                    continue; // anchor already placed
                }
                map.put(gardenTile(pos));
            }
        }

        applyFeatures(map);
        smoothDifficulty(map);

        return map;
    }

    private static IslandTile gardenTile(Position pos) {
        List<AnchorTile> influences = AnchorTiles.all().stream()
                .sorted(Comparator.comparingInt(t -> chebyshev(pos, t.getPosition())))
                .collect(Collectors.toList());

        List<AnchorTile> withinTwo = influences.stream()
                .filter(t -> chebyshev(pos, t.getPosition()) <= 2)
                .toList();

        List<AnchorTile> weighted = withinTwo.isEmpty() ? influences : withinTwo;

        String biome = chooseWeighted(weighted, AnchorTile::getBiome, pos);
        String region = chooseWeighted(weighted, AnchorTile::getRegion, pos);
        String elevation = chooseWeighted(weighted, AnchorTile::getElevation, pos);

        String desc = baseDescription(pos, biome, region, elevation, weighted);
        TileContext ctx = new TileContext(desc);

        return new IslandTile(
                gardenId(pos),
                TileKind.GARDENED,
                pos,
                biome,
                region,
                elevation,
                defaultDifficulty(biome),
                safetyFor(pos),
                safetyFor(pos) != TileSafety.IMPOSSIBLE,
                java.util.Set.of(),
                floraPrimary(biome, region, elevation, pos),
                floraSecondary(biome, region, elevation, pos),
                floraDensity(biome, region, elevation, pos),
                ctx
        );
    }

    private static String gardenId(Position pos) {
        return "G_" + pos.x() + "_" + pos.y();
    }

    private static int chebyshev(Position a, Position b) {
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }

    private static String chooseWeighted(List<AnchorTile> candidates, java.util.function.Function<AnchorTile, String> fn, Position targetPos) {
        Map<String, Integer> counts = candidates.stream().collect(Collectors.toMap(
                fn,
                t -> weightForDistance(t.getPosition(), targetPos),
                Integer::sum
        ));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(fn.apply(candidates.get(0)));
    }

    private static int weightForDistance(Position source, Position target) {
        int dist = chebyshev(source, target);
        if (dist <= 1) return 3;
        if (dist == 2) return 2;
        return 1;
    }

    private static String baseDescription(Position pos, String biome, String region, String elevation, List<AnchorTile> neighbors) {
        String near = neighbors.stream()
                .limit(2)
                .map(AnchorTile::getName)
                .collect(Collectors.joining(" and "));
        return String.format(Locale.ROOT,
                "A %s patch in the %s (%s elevation), roughly between %s.",
                biome.replace('_', ' '), region.replace('_', ' '), elevation, near.isEmpty() ? "other landmarks" : near);
    }

    private static TerrainDifficulty defaultDifficulty(String biome) {
        String b = biome.toLowerCase(Locale.ROOT);
        if (b.contains("vine") || b.contains("bamboo") || b.contains("stream")) {
            return TerrainDifficulty.HARD;
        }
        if (b.contains("waterfall") || b.contains("cliff")) {
            return TerrainDifficulty.EXTREME;
        }
        if (b.contains("light_forest")) {
            return TerrainDifficulty.NORMAL;
        }
        if (b.contains("beach") || b.contains("camp")) {
            return TerrainDifficulty.EASY;
        }
        return TerrainDifficulty.NORMAL;
    }

    private static TileSafety safetyFor(Position pos) {
        // Mark outermost corners of the shoreline as DEAD; top rim edges as IMPOSSIBLE.
        if (pos.y() == WorldGeometry.ISLAND_MIN_Y && Math.abs(pos.x()) >= 3) {
            return TileSafety.DEAD;
        }
        if (pos.y() > 4) {
            return TileSafety.IMPOSSIBLE;
        }
        return TileSafety.NORMAL;
    }

    private static void applyFeatures(IslandMap map) {
        // Mark main path from beach to hill
        for (int y = 0; y <= 4; y++) {
            Position pos = new Position(0, y);
            map.get(pos).ifPresent(tile -> map.put(addFeature(tile, TerrainFeature.PATH, reduceDifficulty(tile))));
        }
        map.get("T_WATERFALL_POOL").ifPresent(tile -> map.put(addFeature(tile, TerrainFeature.WATERFALL_DROP, tile)));
        map.get("T_CAVE_ENTRANCE").ifPresent(tile -> {
            IslandTile withMouth = addFeature(tile, TerrainFeature.CAVE_MOUTH, tile);
            map.put(addFeature(withMouth, TerrainFeature.ROCK_WALL, withMouth));
        });
        map.get("T_VINE_FOREST").ifPresent(tile -> map.put(addFeature(tile, TerrainFeature.TANGLE, tile)));
        map.get("T_STREAM").ifPresent(tile -> map.put(addFeature(tile, TerrainFeature.SLIPPERY_ROCKS, tile)));
        map.get("T_CLIFF_EDGE").ifPresent(tile -> map.put(addFeature(tile, TerrainFeature.CLIFF_FACE, tile)));
    }

    private static IslandTile reduceDifficulty(IslandTile tile) {
        TerrainDifficulty diff = tile.getDifficulty();
        TerrainDifficulty reduced = switch (diff) {
            case EXTREME -> TerrainDifficulty.HARD;
            case HARD -> TerrainDifficulty.NORMAL;
            default -> diff;
        };
        return copyWith(tile, reduced, tile.getFeatures(), tile.getSafety(), tile.isWalkable(),
                tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity());
    }

    private static IslandTile addFeature(IslandTile tile, TerrainFeature feature, IslandTile base) {
        java.util.Set<TerrainFeature> newFeatures = new java.util.HashSet<>(base.getFeatures());
        newFeatures.add(feature);
        return copyWith(base, base.getDifficulty(), newFeatures, base.getSafety(), base.isWalkable(),
                base.getPrimaryPlantFamily(), base.getSecondaryPlantFamilies(), base.getPlantDensity());
    }

    private static IslandTile copyWith(IslandTile tile, TerrainDifficulty difficulty, java.util.Set<TerrainFeature> features,
                                       TileSafety safety, boolean walkable,
                                       PlantFamily primary, java.util.List<PlantFamily> secondary, PlantDensity density) {
        return new IslandTile(
                tile.getTileId(),
                tile.getKind(),
                tile.getPosition(),
                tile.getBiome(),
                tile.getRegion(),
                tile.getElevation(),
                difficulty,
                safety,
                walkable,
                features,
                primary,
                secondary,
                density,
                tile.getContext()
        );
    }

    private static void smoothDifficulty(IslandMap map) {
        java.util.Map<Position, IslandTile> updated = new java.util.HashMap<>();
        java.util.Set<TerrainFeature> abrupt = java.util.EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL);
        for (IslandTile tile : map.allTiles()) {
            if (tile.getSafety() != TileSafety.NORMAL) {
                updated.put(tile.getPosition(), tile);
                continue;
            }
            if (tile.getFeatures().stream().anyMatch(abrupt::contains)) {
                updated.put(tile.getPosition(), tile);
                continue;
            }
            java.util.List<IslandTile> neighbors = new java.util.ArrayList<>();
            for (Direction8 dir : Direction8.values()) {
                map.get(tile.getPosition().step(dir)).ifPresent(neighbor -> {
                    if (neighbor.getSafety() == TileSafety.NORMAL
                            && neighbor.getFeatures().stream().noneMatch(abrupt::contains)) {
                        neighbors.add(neighbor);
                    }
                });
            }
            if (neighbors.isEmpty()) {
                updated.put(tile.getPosition(), tile);
                continue;
            }
            int current = idx(tile.getDifficulty());
            int minAllowed = neighbors.stream().mapToInt(n -> idx(n.getDifficulty()) - 1).max().orElse(current);
            int maxAllowed = neighbors.stream().mapToInt(n -> idx(n.getDifficulty()) + 1).min().orElse(current);
            if (minAllowed > maxAllowed) {
                updated.put(tile.getPosition(), tile);
                continue;
            }
            int clamped = Math.max(minAllowed, Math.min(maxAllowed, current));
            TerrainDifficulty newDiff = TerrainDifficulty.values()[clamped];
            updated.put(tile.getPosition(), copyWith(tile, newDiff, tile.getFeatures(), tile.getSafety(), tile.isWalkable(),
                    tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity()));
        }
        updated.values().forEach(map::put);
    }

    private static int idx(TerrainDifficulty d) {
        return switch (d) {
            case EASY -> 0;
            case NORMAL -> 1;
            case HARD -> 2;
            case EXTREME -> 3;
        };
    }

    private static PlantFamily floraPrimary(String biome, String region, String elevation, Position pos) {
        String b = biome.toLowerCase(Locale.ROOT);
        String r = region.toLowerCase(Locale.ROOT);
        if (b.contains("bamboo")) return PlantFamily.BAMBOO;
        if (b.contains("vine")) return PlantFamily.VINE;
        if (b.contains("waterfall") || b.contains("stream")) return PlantFamily.MOSS;
        if (b.contains("cave")) return PlantFamily.MOSS;
        if (r.contains("shoreline")) return PlantFamily.DUNE_GRASS;
        if (r.contains("high")) return PlantFamily.HIGHLAND_SHRUB;
        return PlantFamily.BROADLEAF_TREE;
    }

    private static java.util.List<PlantFamily> floraSecondary(String biome, String region, String elevation, Position pos) {
        java.util.List<PlantFamily> list = new java.util.ArrayList<>();
        String b = biome.toLowerCase(Locale.ROOT);
        String r = region.toLowerCase(Locale.ROOT);
        if (b.contains("vine")) list.add(PlantFamily.FERN);
        if (b.contains("bamboo")) list.add(PlantFamily.FERN);
        if (b.contains("waterfall")) list.add(PlantFamily.FERN);
        if (b.contains("cave")) list.add(PlantFamily.FUNGUS);
        if (r.contains("shoreline")) list.add(PlantFamily.COASTAL_SHRUB);
        if (list.isEmpty()) list.add(PlantFamily.VINE);
        return list;
    }

    private static PlantDensity floraDensity(String biome, String region, String elevation, Position pos) {
        if (region.toLowerCase(Locale.ROOT).contains("high")) {
            return PlantDensity.SPARSE;
        }
        if (biome.toLowerCase(Locale.ROOT).contains("vine")
                || biome.toLowerCase(Locale.ROOT).contains("bamboo")
                || biome.toLowerCase(Locale.ROOT).contains("waterfall")) {
            return PlantDensity.DENSE;
        }
        if (region.toLowerCase(Locale.ROOT).contains("shoreline")) {
            return PlantDensity.MEDIUM;
        }
        return PlantDensity.MEDIUM;
    }
}
