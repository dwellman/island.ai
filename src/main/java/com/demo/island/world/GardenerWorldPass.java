package com.demo.island.world;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Deterministic Gardener pass used during world build to probe and smooth rough plots.
 */
public final class GardenerWorldPass {

    private GardenerWorldPass() {
    }

    public static GardenerWorldReport run(IslandMap map, GardenerWorldConfig config) {
        GardenerWorldReport report = new GardenerWorldReport();
        // Full coverage deterministic traversal over walkable plots
        Set<String> visited = new HashSet<>();
        Set<String> visitedAnchors = new HashSet<>();
        java.util.List<GardenerVisit> path = new java.util.ArrayList<>();
        exploreFromSpawn(map, visited, visitedAnchors, report, path);
        // Optionally attempt a secondary relaxation pass if unreachable walkables remain
        if (visited.size() < countWalkable(map)) {
            relaxBlockingImpossibles(map, visited, report);
            visited.clear();
            visitedAnchors.clear();
            path.clear();
            exploreFromSpawn(map, visited, visitedAnchors, report, path);
        }

        report.setCoveragePath(java.util.List.copyOf(path));
        report.setWalkablePlotCount(countWalkable(map));
        report.setWalkablePlotsVisited(visited.size());
        report.setUnvisitedWalkablePlots(unvisitedWalkable(map, visited));
        report.setHasFullCoverage(report.getWalkablePlotsVisited() == report.getWalkablePlotCount()
                && report.getUnvisitedWalkablePlots().isEmpty());

        return report;
    }

    private static void exploreFromSpawn(IslandMap map, Set<String> visited, Set<String> visitedAnchors,
                                         GardenerWorldReport report, java.util.List<GardenerVisit> path) {
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(AnchorTiles.startTile().getTileId());
        int index = 0;
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            if (!visited.add(id)) continue;
            IslandTile tile = map.get(id).orElse(null);
            if (tile == null || tile.getSafety() != TileSafety.NORMAL || !tile.isWalkable()) continue;
            report.incrementPlotsVisited();
            path.add(new GardenerVisit(index++, id, tile.getPosition()));
            if (tile.getKind() == TileKind.ANCHOR && visitedAnchors.add(id)) {
                report.incrementAnchorPlotsVisited();
            }
            adjustPathDifficultyIfNeeded(map, tile, report);
            adjustDifficultyOutlierIfNeeded(map, tile, report);
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n == null) continue;
                if (n.getSafety() != TileSafety.NORMAL || !n.isWalkable()) {
                    relaxSafetyIfNeeded(map, n, report);
                    continue;
                }
                if (!visited.contains(n.getTileId())) {
                    queue.add(n.getTileId());
                }
            }
        }
    }

    private static int countWalkable(IslandMap map) {
        return (int) map.allTiles().stream()
                .filter(t -> t.getSafety() == TileSafety.NORMAL && t.isWalkable())
                .count();
    }

    private static java.util.List<String> unvisitedWalkable(IslandMap map, Set<String> visited) {
        return map.allTiles().stream()
                .filter(t -> t.getSafety() == TileSafety.NORMAL && t.isWalkable())
                .filter(t -> !visited.contains(t.getTileId()))
                .map(IslandTile::getTileId)
                .toList();
    }

    private static void relaxBlockingImpossibles(IslandMap map, Set<String> visited, GardenerWorldReport report) {
        // Relax a limited number of IMPOSSIBLE tiles adjacent to visited walkable tiles
        int relaxed = 0;
        for (IslandTile tile : map.allTiles()) {
            if (tile.getSafety() != TileSafety.IMPOSSIBLE || tile.getKind() != TileKind.GARDENED) continue;
            boolean touchesVisited = false;
            for (Direction8 dir : Direction8.values()) {
                IslandTile neighbor = map.get(tile.getPosition().step(dir)).orElse(null);
                if (neighbor != null && neighbor.getSafety() == TileSafety.NORMAL && visited.contains(neighbor.getTileId())) {
                    touchesVisited = true;
                    break;
                }
            }
            if (touchesVisited) {
                relaxSafetyIfNeeded(map, tile, report);
                relaxed++;
            }
            if (relaxed >= 10) {
                break; // avoid over-relaxing
            }
        }
    }

    private static MoveOutcome evaluateMove(IslandMap map, Position targetPos, IslandTile targetTile) {
        if (targetTile == null) {
            return MoveOutcome.MOVE_BLOCKED_NO_TILE;
        }
        WorldGeometry.Classification cls = WorldGeometry.classify(targetPos);
        if (cls == WorldGeometry.Classification.OFF_WORLD || cls == WorldGeometry.Classification.BOUNDARY) {
            return MoveOutcome.MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY;
        }
        if (targetTile.getSafety() == TileSafety.IMPOSSIBLE) {
            return MoveOutcome.MOVE_BLOCKED_IMPOSSIBLE;
        }
        if (targetTile.getSafety() == TileSafety.DEAD) {
            return MoveOutcome.MOVE_FATAL_DEAD_TILE;
        }
        return MoveOutcome.MOVE_OK;
    }

    private static void relaxSafetyIfNeeded(IslandMap map, IslandTile tile, GardenerWorldReport report) {
        if (tile.getKind() != TileKind.GARDENED) {
            return;
        }
        TileSafety original = tile.getSafety();
        if (original == TileSafety.DEAD) {
            IslandTile relaxed = cloneWith(tile, tile.getDifficulty(), tile.getFeatures(), TileSafety.NORMAL, true,
                    tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity());
            map.put(relaxed);
            report.incrementDeadPlotsRelaxed();
        } else if (original == TileSafety.IMPOSSIBLE) {
            IslandTile relaxed = cloneWith(tile, tile.getDifficulty(), tile.getFeatures(), TileSafety.NORMAL, true,
                    tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity());
            map.put(relaxed);
            report.incrementImpossiblePlotsRelaxed();
        }
    }

    private static void adjustPathDifficultyIfNeeded(IslandMap map, IslandTile tile, GardenerWorldReport report) {
        if (tile.getKind() != TileKind.GARDENED) return;
        if (!tile.getFeatures().contains(TerrainFeature.PATH)) return;
        TerrainDifficulty diff = tile.getDifficulty();
        if (diff == TerrainDifficulty.HARD || diff == TerrainDifficulty.EXTREME) {
            TerrainDifficulty reduced = (diff == TerrainDifficulty.EXTREME) ? TerrainDifficulty.HARD : TerrainDifficulty.NORMAL;
            IslandTile updated = cloneWith(tile, reduced, tile.getFeatures(), tile.getSafety(), tile.isWalkable(),
                    tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity());
            map.put(updated);
            report.incrementPathPlotsSmoothed();
        }
    }

    private static void adjustDifficultyOutlierIfNeeded(IslandMap map, IslandTile tile, GardenerWorldReport report) {
        if (tile.getKind() != TileKind.GARDENED) return;
        if (tile.getSafety() != TileSafety.NORMAL) return;
        if (tile.getFeatures().stream().anyMatch(f -> EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL).contains(f))) {
            return;
        }
        int currentIdx = idx(tile.getDifficulty());
        int minAllowed = currentIdx;
        int maxAllowed = currentIdx;
        boolean hasNeighbor = false;
        for (Direction8 dir : Direction8.values()) {
            IslandTile neighbor = map.get(tile.getPosition().step(dir)).orElse(null);
            if (neighbor == null) continue;
            if (neighbor.getSafety() != TileSafety.NORMAL) continue;
            if (neighbor.getFeatures().stream().anyMatch(f -> EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL).contains(f))) {
                continue;
            }
            hasNeighbor = true;
            int nIdx = idx(neighbor.getDifficulty());
            minAllowed = Math.max(minAllowed, nIdx - 1);
            maxAllowed = Math.min(maxAllowed, nIdx + 1);
        }
        if (!hasNeighbor) {
            return;
        }
        if (minAllowed > maxAllowed) {
            return;
        }
        if (currentIdx < minAllowed || currentIdx > maxAllowed) {
            int clamped = Math.max(minAllowed, Math.min(maxAllowed, currentIdx));
            TerrainDifficulty newDiff = TerrainDifficulty.values()[clamped];
            IslandTile updated = cloneWith(tile, newDiff, tile.getFeatures(), tile.getSafety(), tile.isWalkable(),
                    tile.getPrimaryPlantFamily(), tile.getSecondaryPlantFamilies(), tile.getPlantDensity());
            map.put(updated);
            report.incrementDifficultyOutliersFixed();
        }
    }

    private static int idx(TerrainDifficulty d) {
        return switch (d) {
            case EASY -> 0;
            case NORMAL -> 1;
            case HARD -> 2;
            case EXTREME -> 3;
        };
    }

    private static IslandTile cloneWith(IslandTile tile, TerrainDifficulty difficulty, Set<TerrainFeature> features,
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

    private static boolean isFullyConnected(IslandMap map) {
        Set<Position> visited = new HashSet<>();
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
        Position start = AnchorTiles.startTile().getPosition();
        queue.add(start);
        while (!queue.isEmpty()) {
            Position pos = queue.removeFirst();
            if (!visited.add(pos)) continue;
            IslandTile tile = map.get(pos).orElse(null);
            if (tile == null || !tile.isWalkable()) {
                continue;
            }
            for (Direction8 dir : Direction8.values()) {
                Position next = pos.step(dir);
                IslandTile maybe = map.get(next).orElse(null);
                if (maybe != null && maybe.isWalkable() && !visited.contains(next)) {
                    queue.add(next);
                }
            }
        }
        long walkableCount = map.allTiles().stream().filter(IslandTile::isWalkable).count();
        long visitedWalkable = visited.stream().filter(p -> map.get(p).map(IslandTile::isWalkable).orElse(false)).count();
        return visitedWalkable == walkableCount;
    }
}
