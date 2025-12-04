package com.demo.island.core;

import java.util.Objects;

public final class ItemInstance {

    public enum OwnerKind {
        TILE,
        PLAYER,
        CREATURE,
        ITEM
    }

    private final String itemId;
    private final String itemTypeId;
    private OwnerKind ownerKind;
    private String ownerId;
    private String containedByItemId;
    private final Stats stats = new Stats();
    private TextFace textFace;

    public ItemInstance(String itemId, String itemTypeId, OwnerKind ownerKind, String ownerId, TextFace textFace) {
        this.itemId = Objects.requireNonNull(itemId);
        this.itemTypeId = Objects.requireNonNull(itemTypeId);
        this.ownerKind = Objects.requireNonNull(ownerKind);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.textFace = Objects.requireNonNull(textFace);
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemTypeId() {
        return itemTypeId;
    }

    public OwnerKind getOwnerKind() {
        return ownerKind;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void changeOwner(OwnerKind newOwnerKind, String newOwnerId) {
        this.ownerKind = Objects.requireNonNull(newOwnerKind);
        this.ownerId = Objects.requireNonNull(newOwnerId);
        this.containedByItemId = null;
    }

    public String getContainedByItemId() {
        return containedByItemId;
    }

    public void setContainedByItemId(String containedByItemId) {
        this.containedByItemId = containedByItemId;
    }

    public Stats getStats() {
        return stats;
    }

    public TextFace getTextFace() {
        return textFace;
    }

    public void setTextFace(TextFace textFace) {
        this.textFace = Objects.requireNonNull(textFace);
    }
}
