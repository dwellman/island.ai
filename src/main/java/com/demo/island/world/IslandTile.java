package com.demo.island.world;

/**
 * Unified tile representation for anchors and gardened tiles.
 */
public final class IslandTile {

    private final String tileId;
    private final TileKind kind;
    private final Position position;
    private final String biome;
    private final String region;
    private final String elevation;
    private final TerrainDifficulty difficulty;
    private final TileSafety safety;
    private final boolean walkable;
    private final java.util.Set<TerrainFeature> features;
    private final PlantFamily primaryPlantFamily;
    private final java.util.List<PlantFamily> secondaryPlantFamilies;
    private final PlantDensity plantDensity;
    private final TileContext context;

    public IslandTile(String tileId, TileKind kind, Position position, String biome, String region, String elevation,
                      TerrainDifficulty difficulty, TileSafety safety, boolean walkable,
                      java.util.Set<TerrainFeature> features,
                      PlantFamily primaryPlantFamily,
                      java.util.List<PlantFamily> secondaryPlantFamilies,
                      PlantDensity plantDensity,
                      TileContext context) {
        this.tileId = tileId;
        this.kind = kind;
        this.position = position;
        this.biome = biome;
        this.region = region;
        this.elevation = elevation;
        this.difficulty = difficulty;
        this.safety = safety;
        this.walkable = walkable;
        this.features = features == null ? java.util.Set.of() : java.util.Set.copyOf(features);
        this.primaryPlantFamily = primaryPlantFamily;
        this.secondaryPlantFamilies = secondaryPlantFamilies == null ? java.util.List.of() : java.util.List.copyOf(secondaryPlantFamilies);
        this.plantDensity = plantDensity;
        this.context = context;
    }

    public String getTileId() {
        return tileId;
    }

    public TileKind getKind() {
        return kind;
    }

    public Position getPosition() {
        return position;
    }

    public String getBiome() {
        return biome;
    }

    public String getRegion() {
        return region;
    }

    public String getElevation() {
        return elevation;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public TerrainDifficulty getDifficulty() {
        return difficulty;
    }

    public TileSafety getSafety() {
        return safety;
    }

    public java.util.Set<TerrainFeature> getFeatures() {
        return features;
    }

    public PlantFamily getPrimaryPlantFamily() {
        return primaryPlantFamily;
    }

    public java.util.List<PlantFamily> getSecondaryPlantFamilies() {
        return secondaryPlantFamilies;
    }

    public PlantDensity getPlantDensity() {
        return plantDensity;
    }

    public TileContext getContext() {
        return context;
    }
}
