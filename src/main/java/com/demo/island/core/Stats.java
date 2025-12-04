package com.demo.island.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Stats {

    private final Map<String, Integer> values = new HashMap<>();

    public void set(String name, int value) {
        values.put(Objects.requireNonNull(name), value);
    }

    public int getOrDefault(String name, int defaultValue) {
        return values.getOrDefault(name, defaultValue);
    }

    public boolean has(String name) {
        return values.containsKey(name);
    }

    public Map<String, Integer> asUnmodifiableMap() {
        return Collections.unmodifiableMap(values);
    }
}
