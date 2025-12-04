package com.demo.island.dto;

import com.demo.island.world.Direction8;
import com.demo.island.world.PlantDensity;
import com.demo.island.world.PlantFamily;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TerrainFeature;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlotContext {
    public String plotId;
    public String biome;
    public String region;
    public String elevation;
    public TerrainDifficulty terrainDifficulty;
    public Set<TerrainFeature> terrainFeatures;
    public PlantFamily floraPrimary;
    public PlantDensity floraDensity;
    public String currentDescription;
    public Map<Direction8, String> neighborSummaries;
    public List<ThingContext> visibleThings;
    public List<ThingContext> hiddenThings;
}
