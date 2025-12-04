package com.demo.island.world;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable anchor tile definition with position, environment tags, flags, and neighbor ids.
 */
public final class AnchorTile {

    private final String tileId;
    private final String name;
    private final String description;
    private final Position position;
    private final String biome;
    private final String region;
    private final String elevation;
    private final boolean isStartTile;
    private final boolean isExitCandidate;
    private final boolean isHazard;
    private final boolean isSecret;
    private final boolean isHub;
    private final Map<Direction8, String> neighbors;

    public AnchorTile(String tileId,
                      String name,
                      String description,
                      Position position,
                      String biome,
                      String region,
                      String elevation,
                      boolean isStartTile,
                      boolean isExitCandidate,
                      boolean isHazard,
                      boolean isSecret,
                      boolean isHub,
                      Map<Direction8, String> neighbors) {
        this.tileId = Objects.requireNonNull(tileId);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.position = Objects.requireNonNull(position);
        this.biome = Objects.requireNonNull(biome);
        this.region = Objects.requireNonNull(region);
        this.elevation = Objects.requireNonNull(elevation);
        this.isStartTile = isStartTile;
        this.isExitCandidate = isExitCandidate;
        this.isHazard = isHazard;
        this.isSecret = isSecret;
        this.isHub = isHub;
        this.neighbors = neighbors == null ? Map.of() : Map.copyOf(neighbors);
    }

    public String getTileId() {
        return tileId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public boolean isStartTile() {
        return isStartTile;
    }

    public boolean isExitCandidate() {
        return isExitCandidate;
    }

    public boolean isHazard() {
        return isHazard;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public boolean isHub() {
        return isHub;
    }

    public Map<Direction8, String> getNeighbors() {
        return neighbors;
    }

    public String getNeighbor(Direction8 direction) {
        return neighbors.get(direction);
    }

    public static Map<Direction8, String> neighborsOf(Map.Entry<Direction8, String>... entries) {
        Map<Direction8, String> map = new EnumMap<>(Direction8.class);
        for (Map.Entry<Direction8, String> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Map.copyOf(map);
    }
}
