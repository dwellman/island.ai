package com.demo.island.dto;

import java.util.List;
import java.util.Map;

public final class TileSnapshot {

    private final String tileId;
    private final String biome;
    private final String region;
    private final String name;
    private final String shortDescription;
    private final String detailDescription;
    private final String history;
    private final boolean discovered;
    private final List<TileEventDto> recentEvents;
    private final Map<String, String> neighbors;
    private final List<ItemSnapshot> items;

    public TileSnapshot(String tileId, String biome, String region, String name, String shortDescription,
                        String detailDescription, String history, boolean discovered, Map<String, String> neighbors,
                        List<ItemSnapshot> items, List<TileEventDto> recentEvents) {
        this.tileId = tileId;
        this.biome = biome;
        this.region = region;
        this.name = name;
        this.shortDescription = shortDescription;
        this.detailDescription = detailDescription;
        this.history = history;
        this.discovered = discovered;
        this.neighbors = neighbors;
        this.items = items;
        this.recentEvents = recentEvents;
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

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public String getHistory() {
        return history;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public Map<String, String> getNeighbors() {
        return neighbors;
    }

    public List<ItemSnapshot> getItems() {
        return items;
    }

    public List<TileEventDto> getRecentEvents() {
        return recentEvents;
    }
}
