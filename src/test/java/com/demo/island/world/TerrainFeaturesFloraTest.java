package com.demo.island.world;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TerrainFeaturesFloraTest {

    @Test
    void difficultyIsSmoothedBetweenNeighbors() {
        IslandMap map = IslandGardener.garden();
        EnumSet<TerrainFeature> abrupt = EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL);

        for (IslandTile tile : map.allTiles()) {
            if (tile.getSafety() != TileSafety.NORMAL || tile.getFeatures().stream().anyMatch(abrupt::contains)) {
                continue;
            }
            int idx = idx(tile.getDifficulty());
            for (Direction8 dir : Direction8.values()) {
                IslandTile neighbor = map.get(tile.getPosition().step(dir)).orElse(null);
                if (neighbor == null || neighbor.getSafety() != TileSafety.NORMAL || neighbor.getFeatures().stream().anyMatch(abrupt::contains)) {
                    continue;
                }
                int nIdx = idx(neighbor.getDifficulty());
                assertThat(Math.abs(idx - nIdx))
                        .as("Diff between %s and %s", tile.getTileId(), neighbor.getTileId())
                        .isLessThanOrEqualTo(1);
            }
        }
    }

    @Test
    void mainPathMarked() {
        IslandMap map = IslandGardener.garden();
        for (int y = 0; y <= 4; y++) {
            IslandTile tile = map.get(new Position(0, y)).orElseThrow();
            assertThat(tile.getFeatures()).contains(TerrainFeature.PATH);
        }
    }

    @Test
    void floraConsistency() {
        IslandMap map = IslandGardener.garden();

        // Shoreline tiles use coastal flora
        for (IslandTile tile : map.allTiles()) {
            if ("shoreline".equalsIgnoreCase(tile.getRegion())) {
                assertThat(tile.getPrimaryPlantFamily()).isIn(PlantFamily.DUNE_GRASS, PlantFamily.COASTAL_SHRUB);
            }
        }

        // Bamboo grove primary is bamboo
        IslandTile bamboo = map.get("T_BAMBOO_GROVE").orElseThrow();
        assertThat(bamboo.getPrimaryPlantFamily()).isEqualTo(PlantFamily.BAMBOO);

        // Vine forest includes vine
        IslandTile vines = map.get("T_VINE_FOREST").orElseThrow();
        boolean hasVine = vines.getPrimaryPlantFamily() == PlantFamily.VINE
                || vines.getSecondaryPlantFamilies().contains(PlantFamily.VINE);
        assertThat(hasVine).isTrue();

        // High rim tiles not dense
        for (IslandTile tile : map.allTiles()) {
            if ("high_ground".equalsIgnoreCase(tile.getRegion())) {
                assertThat(tile.getPlantDensity()).isNotEqualTo(PlantDensity.DENSE);
                assertThat(tile.getPrimaryPlantFamily()).isIn(PlantFamily.HIGHLAND_SHRUB, PlantFamily.LICHEN);
            }
        }
    }

    private int idx(TerrainDifficulty d) {
        return switch (d) {
            case EASY -> 0;
            case NORMAL -> 1;
            case HARD -> 2;
            case EXTREME -> 3;
        };
    }
}
