package com.demo.island.game;

import java.util.EnumMap;
import java.util.Map;

public final class PlotResources {
    private final Map<GameItemType, Integer> items = new EnumMap<>(GameItemType.class);
    private boolean discovered;

    public PlotResources(boolean discovered) {
        this.discovered = discovered;
    }

    public Map<GameItemType, Integer> getItems() {
        return items;
    }

    public void add(GameItemType type, int count) {
        items.put(type, items.getOrDefault(type, 0) + count);
    }

    public boolean has(GameItemType type) {
        return items.getOrDefault(type, 0) > 0;
    }

    public boolean take(GameItemType type) {
        int current = items.getOrDefault(type, 0);
        if (current <= 0) return false;
        items.put(type, current - 1);
        return true;
    }

    public void drop(GameItemType type) {
        add(type, 1);
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }
}
