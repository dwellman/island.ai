package com.demo.island.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Structured summary of the world build, prior to any game-layer logic.
 */
public final class IslandCreationReport {
    public static final class Meta {
        public String worldId;
        public String buildVersion;
        public long gardenerSeed;
    }

    public static final class GeometrySummary {
        public int worldMinX;
        public int worldMaxX;
        public int worldMinY;
        public int worldMaxY;
        public int islandMinX;
        public int islandMaxX;
        public int islandMinY;
        public int islandMaxY;
        public int islandEligiblePlotCount;
    }

    public static final class AnchorSummary {
        public String anchorId;
        public Position position;
        public String biome;
        public String region;
        public String elevation;
        public boolean isSpawn;
        public boolean isExitCandidate;
        public boolean isHazard;
        public boolean isSecret;
        public boolean isHub;
        public int shortestStepsFromSpawn;
        public int minDifficultyScoreFromSpawn;
    }

    public static final class ConnectivitySummary {
        public boolean allPrimePlotsReachableFromSpawn;
        public List<String> unreachablePrimePlots;
        public int walkablePlotsReachableFromSpawn;
        public int totalWalkablePlots;
        public int unreachableWalkablePlots;
        public List<AnchorSummary> anchorPaths;
    }

    public static final class HazardSummary {
        public int normalPlotCount;
        public int deadPlotCount;
        public int impossiblePlotCount;
        public Map<String, Integer> deadPlotsByRegion;
        public Map<String, Integer> impossiblePlotsByRegion;
        public Map<String, Boolean> anchorHasSafePath;
        public Map<String, Boolean> anchorRequiresHazard;
    }

    public static final class DifficultySummary {
        public Map<TerrainDifficulty, Integer> difficultyCounts;
        public int neighborDifficultyViolations;
        public int maxNeighborDifficultyDifference;
    }

    public static final class FeatureSummary {
        public Map<TerrainFeature, Integer> featureCounts;
        public boolean mainPathDefined;
        public boolean mainPathContinuous;
        public boolean mainPathAllPlotsHavePath;
        public int mainPathTotalSteps;
        public int mainPathTotalDifficultyScore;
    }

    public static final class FloraSummary {
        public Map<PlantFamily, Integer> plantFamilyCounts;
        public Map<PlantDensity, Integer> plantDensityCounts;
    }

    public static final class GardenerSummary {
        public int plotsVisitedByGardener;
        public int anchorPlotsVisitedByGardener;
        public int walkablePlotsVisitedByGardener;
        public java.util.List<String> unvisitedWalkablePlots;
        public boolean hasFullGardenerCoverage;
        public java.util.List<GardenerVisit> coveragePath;
        public int difficultyOutliersFixedByGardener;
        public int deadPlotsRelaxedByGardener;
        public int impossiblePlotsRelaxedByGardener;
        public int pathPlotsSmoothedByGardener;
        public List<String> warnings;
    }

    public static final class Verdict {
        public boolean readyForCosmos;
        public List<String> blockingIssues;
        public List<String> nonBlockingWarnings;
    }

    public Meta meta;
    public GeometrySummary geometry;
    public int anchorPlotCount;
    public List<AnchorSummary> anchors;
    public List<String> exitCandidatePlotIds;
    public String spawnPlotId;
    public int walkablePlotCount;
    public ConnectivitySummary connectivity;
    public HazardSummary hazards;
    public DifficultySummary difficulty;
    public FeatureSummary features;
    public FloraSummary flora;
    public GardenerSummary gardener;
    public Verdict verdict;

    public static IslandCreationReport empty() {
        IslandCreationReport r = new IslandCreationReport();
        r.meta = new Meta();
        r.geometry = new GeometrySummary();
        r.connectivity = new ConnectivitySummary();
        r.hazards = new HazardSummary();
        r.difficulty = new DifficultySummary();
        r.features = new FeatureSummary();
        r.flora = new FloraSummary();
        r.gardener = new GardenerSummary();
        r.verdict = new Verdict();
        r.anchors = List.of();
        r.exitCandidatePlotIds = List.of();
        r.connectivity.unreachablePrimePlots = List.of();
        r.connectivity.anchorPaths = List.of();
        r.walkablePlotCount = 0;
        r.hazards.deadPlotsByRegion = Collections.emptyMap();
        r.hazards.impossiblePlotsByRegion = Collections.emptyMap();
        r.hazards.anchorHasSafePath = Collections.emptyMap();
        r.hazards.anchorRequiresHazard = Collections.emptyMap();
        r.difficulty.difficultyCounts = Collections.emptyMap();
        r.features.featureCounts = Collections.emptyMap();
        r.flora.plantFamilyCounts = Collections.emptyMap();
        r.flora.plantDensityCounts = Collections.emptyMap();
        r.gardener.warnings = List.of();
        r.gardener.unvisitedWalkablePlots = List.of();
        r.gardener.coveragePath = List.of();
        r.verdict.blockingIssues = List.of();
        r.verdict.nonBlockingWarnings = List.of();
        return r;
    }
}
