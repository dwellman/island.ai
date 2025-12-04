package com.demo.island.dto;

import java.util.List;
import java.util.Map;

public final class ItemSnapshot {

    private final String itemId;
    private final String itemTypeId;
    private final String ownerKind;
    private final String ownerId;
    private final String containedByItemId;
    private final String name;
    private final String shortDescription;
    private final List<String> tags;
    private final Map<String, Integer> stats;
    private final String detailDescription;
    private final String history;

    public ItemSnapshot(String itemId, String itemTypeId, String ownerKind, String ownerId, String containedByItemId,
                        String name, String shortDescription, String detailDescription, String history,
                        List<String> tags, Map<String, Integer> stats) {
        this.itemId = itemId;
        this.itemTypeId = itemTypeId;
        this.ownerKind = ownerKind;
        this.ownerId = ownerId;
        this.containedByItemId = containedByItemId;
        this.name = name;
        this.shortDescription = shortDescription;
        this.detailDescription = detailDescription;
        this.history = history;
        this.tags = tags;
        this.stats = stats;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemTypeId() {
        return itemTypeId;
    }

    public String getOwnerKind() {
        return ownerKind;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getContainedByItemId() {
        return containedByItemId;
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

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }
}
