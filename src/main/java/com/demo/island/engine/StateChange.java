package com.demo.island.engine;

import com.demo.island.core.WorldState;

public interface StateChange {

    void applyTo(WorldState worldState);
}
