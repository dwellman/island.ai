package com.demo.island.core;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Tile {

    public enum Direction {
        N, NE, E, SE, S, SW, W, NW
    }

    private final String tileId;
    private final String biome;
    private final String region;
    private final Map<Direction, String> neighbors = new EnumMap<>(Direction.class);
    private TextFace textFace;
    private boolean discovered;
    private static final int MAX_EVENT_HISTORY = 20;
    private final LinkedList<TileEvent> recentEvents = new LinkedList<>();

    public Tile(String tileId, String biome, String region, TextFace textFace) {
        this.tileId = Objects.requireNonNull(tileId);
        this.biome = Objects.requireNonNull(biome);
        this.region = Objects.requireNonNull(region);
        this.textFace = Objects.requireNonNull(textFace);
        this.discovered = false;
    }

    public String getTileId() {
        return tileId;
    }

    public String getBiome() {
        return biome;
    }

    public String getRegion() {
        return region;
    }

    public TextFace getTextFace() {
        return textFace;
    }

    public void setTextFace(TextFace textFace) {
        this.textFace = Objects.requireNonNull(textFace);
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public void connect(Direction direction, String neighborTileId) {
        neighbors.put(Objects.requireNonNull(direction), Objects.requireNonNull(neighborTileId));
    }

    public String getNeighbor(Direction direction) {
        return neighbors.get(direction);
    }

    public Map<Direction, String> getNeighbors() {
        return neighbors;
    }

    public void recordEvent(TileEvent event) {
        if (event == null) {
            return;
        }
        recentEvents.addFirst(event);
        while (recentEvents.size() > MAX_EVENT_HISTORY) {
            recentEvents.removeLast();
        }
    }

    public List<TileEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }
}
