package com.demo.island.world;

import java.util.HashSet;
import java.util.Set;

public class Thing {
    private final String id;
    private final String name;
    private final ThingKind kind;
    private String currentPlotId;
    private final Set<String> homePlotIds = new HashSet<>();
    private final Set<String> tags = new HashSet<>();

    public Thing(String id, String name, ThingKind kind, String currentPlotId) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.currentPlotId = currentPlotId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ThingKind getKind() {
        return kind;
    }

    public String getCurrentPlotId() {
        return currentPlotId;
    }

    public void setCurrentPlotId(String currentPlotId) {
        this.currentPlotId = currentPlotId;
    }

    public Set<String> getHomePlotIds() {
        return homePlotIds;
    }

    public Set<String> getTags() {
        return tags;
    }
}
