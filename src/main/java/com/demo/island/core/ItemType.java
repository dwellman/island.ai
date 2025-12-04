package com.demo.island.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ItemType {

    private final String itemTypeId;
    private final TextFace baseText;
    private final List<String> tags;
    private final List<String> actionHooks;

    public ItemType(String itemTypeId, TextFace baseText, List<String> tags, List<String> actionHooks) {
        this.itemTypeId = Objects.requireNonNull(itemTypeId);
        this.baseText = Objects.requireNonNull(baseText);
        this.tags = List.copyOf(tags == null ? List.of() : tags);
        this.actionHooks = List.copyOf(actionHooks == null ? List.of() : actionHooks);
    }

    public String getItemTypeId() {
        return itemTypeId;
    }

    public TextFace getBaseText() {
        return baseText;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public List<String> getActionHooks() {
        return actionHooks;
    }

    public ItemType withAdditionalHook(String hookId) {
        List<String> merged = new ArrayList<>(actionHooks);
        merged.add(hookId);
        return new ItemType(itemTypeId, baseText, tags, merged);
    }
}
