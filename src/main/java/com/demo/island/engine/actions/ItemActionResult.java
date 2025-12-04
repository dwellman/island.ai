package com.demo.island.engine.actions;

import com.demo.island.engine.StateChange;

import java.util.ArrayList;
import java.util.List;

public final class ItemActionResult {

    private final String narration;
    private final List<StateChange> stateChanges = new ArrayList<>();

    public ItemActionResult(String narration) {
        this.narration = narration;
    }

    public String getNarration() {
        return narration;
    }

    public List<StateChange> getStateChanges() {
        return stateChanges;
    }

    public ItemActionResult addChange(StateChange change) {
        stateChanges.add(change);
        return this;
    }
}
