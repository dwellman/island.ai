package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.dto.ThingContext;
import com.demo.island.world.Direction8;
import com.demo.island.world.GardenerWorldConfig;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.WorldThingIndex;
import com.demo.island.world.Thing;
import com.demo.island.world.ThingKind;
import com.demo.island.world.TerrainFeature;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ContextBuilder {

    private ContextBuilder() {
    }

    public static PlotContext buildPlotContext(GameSession session) {
        PlotContext ctx = new PlotContext();
        IslandMap map = session.getMap();
        String plotId = session.getLocation().getTileId();
        IslandTile tile = map.get(plotId).orElseThrow();

        ctx.plotId = tile.getTileId();
        ctx.biome = tile.getBiome();
        ctx.region = tile.getRegion();
        ctx.elevation = tile.getElevation();
        ctx.terrainDifficulty = tile.getDifficulty();
        ctx.terrainFeatures = tile.getFeatures();
        ctx.floraPrimary = tile.getPrimaryPlantFamily();
        ctx.floraDensity = tile.getPlantDensity();
        ctx.currentDescription = tile.getContext().getCurrentDescription();
        ctx.neighborSummaries = buildNeighborSummaries(map, tile);

        WorldThingIndex index = session.getThingIndex();
        List<ThingContext> visible = new ArrayList<>();
        List<ThingContext> hidden = new ArrayList<>();
        for (Thing t : index.getThingsInPlot(plotId)) {
            boolean visibleToPlayer = isVisible(t);
            ThingContext tc = buildThingContext(t, visibleToPlayer);
            if (visibleToPlayer) {
                visible.add(tc);
            } else {
                hidden.add(tc);
            }
        }
        ctx.visibleThings = visible;
        ctx.hiddenThings = hidden;
        return ctx;
    }

    private static Map<Direction8, String> buildNeighborSummaries(IslandMap map, IslandTile tile) {
        Map<Direction8, String> summaries = new EnumMap<>(Direction8.class);
        for (Direction8 dir : Direction8.values()) {
            map.get(tile.getPosition().step(dir)).ifPresent(neighbor -> {
                summaries.put(dir, neighborSummary(neighbor, dir));
            });
        }
        return summaries;
    }

    private static String neighborSummary(IslandTile neighbor, Direction8 dir) {
        String biome = neighbor.getBiome().replace('_', ' ');
        if (neighbor.getFeatures().contains(TerrainFeature.PATH)) {
            return "Path " + dir.name() + " toward " + biome;
        }
        return biome + " lies " + dir.name();
    }

    private static boolean isVisible(Thing t) {
        if (t.getId().equals("THING_PLAYER")) return true;
        if (t.getKind() == ThingKind.ITEM) return true;
        if (t.getTags().contains("INVISIBLE") || t.getTags().contains("HIDDEN")) {
            return false;
        }
        return true;
    }

    private static ThingContext buildThingContext(Thing t, boolean visibleToPlayer) {
        ThingContext tc = new ThingContext();
        tc.setId(t.getId());
        tc.setName(t.getName());
        tc.setKind(t.getKind());
        tc.setLocationPlotId(t.getCurrentPlotId());
        tc.setVisibleToPlayer(visibleToPlayer);
        tc.setShortDescription(simpleShortDescription(t));
        tc.setBehaviorHint(simpleBehaviorHint(t));
        tc.setStatsSummary(simpleStats(t));
        tc.setGoals(simpleGoals(t));
        tc.setTriggers(simpleTriggers(t));
        tc.setSecrets(simpleSecrets(t));
        return tc;
    }

    private static String simpleShortDescription(Thing t) {
        if (t.getKind() == ThingKind.CHARACTER && t.getTags().contains("MONKEY_TROOP")) {
            return "A cluster of monkeys watching you from the branches.";
        }
        if (t.getKind() == ThingKind.CHARACTER && t.getTags().contains("GHOST")) {
            return "A drifting ghostly shape lingering near the ruins.";
        }
        if (t.getKind() == ThingKind.ITEM) {
            return "An item lies here.";
        }
        return "You see " + t.getName();
    }

    private static String simpleBehaviorHint(Thing t) {
        if (t.getTags().contains("MONKEY_TROOP")) {
            return "They seem restless and alert.";
        }
        if (t.getTags().contains("GHOST")) {
            return "It seems to watch quietly.";
        }
        return "";
    }

    private static String simpleStats(Thing t) {
        if (t instanceof com.demo.island.world.CharacterThing ct) {
            return "HP " + ct.getHp() + "/" + ct.getMaxHp();
        }
        return "";
    }

    private static List<String> simpleGoals(Thing t) {
        if (t.getTags().contains("GHOST")) {
            return List.of("Guard its lair.");
        }
        if (t.getTags().contains("MONKEY_TROOP")) {
            return List.of("Protect their vines.");
        }
        return List.of();
    }

    private static List<String> simpleTriggers(Thing t) {
        if (t.getTags().contains("GHOST")) {
            return List.of("May react strongly at night.");
        }
        return List.of();
    }

    private static List<String> simpleSecrets(Thing t) {
        if (t.getTags().contains("GHOST")) {
            return List.of("The machete belonged to the Gardener.");
        }
        return List.of();
    }
}
