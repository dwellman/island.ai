package com.demo.island.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldThingIndex {

    private final IslandMap map;
    private final Map<String, Thing> thingsById = new HashMap<>();

    public WorldThingIndex(IslandMap map) {
        this.map = map;
    }

    public void registerThing(Thing thing) {
        thingsById.put(thing.getId(), thing);
        if (thing.getCurrentPlotId() != null) {
            map.get(thing.getCurrentPlotId()).ifPresent(tile -> tile.getThingsPresent().add(thing.getId()));
        }
        for (String home : thing.getHomePlotIds()) {
            map.get(home).ifPresent(tile -> tile.getThingsAnchoredHere().add(thing.getId()));
        }
    }

    public Thing getThing(String id) {
        return thingsById.get(id);
    }

    public List<Thing> getThingsInPlot(String plotId) {
        List<Thing> list = new ArrayList<>();
        map.get(plotId).ifPresent(tile -> {
            for (String tid : tile.getThingsPresent()) {
                Thing t = thingsById.get(tid);
                if (t != null) {
                    list.add(t);
                }
            }
        });
        return list;
    }

    public void moveThing(String thingId, String newPlotId) {
        Thing thing = thingsById.get(thingId);
        if (thing == null) return;
        String old = thing.getCurrentPlotId();
        if (old != null) {
            map.get(old).ifPresent(tile -> tile.getThingsPresent().remove(thingId));
        }
        thing.setCurrentPlotId(newPlotId);
        if (newPlotId != null) {
            map.get(newPlotId).ifPresent(tile -> tile.getThingsPresent().add(thingId));
        }
    }

    public Map<String, Thing> getAll() {
        return thingsById;
    }
}
