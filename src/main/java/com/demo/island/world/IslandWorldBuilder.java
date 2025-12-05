package com.demo.island.world;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Orchestrates world build steps and logs each phase. Does not start the game clock.
 */
public final class IslandWorldBuilder {

    private static final Logger LOG = LogManager.getLogger(IslandWorldBuilder.class);

    private IslandWorldBuilder() {
    }

    public static IslandCreationResult buildWorldWithLogging() {
        logStepHeader(1, "Define world geometry and bounds");
        logGeometry();

        logStepHeader(2, "Define prime (anchor) plots with IDs, coordinates, and roles");
        logAnchors();

        logStepHeader(3, "Wire anchor topology (8-way neighbors between anchor plots)");
        logAnchorTopology();

        logStepHeader(4, "Garden the full island band into plots using neighbor influence");
        IslandMap map = IslandGardener.garden();
        logGardenedMap(map);

        logStepHeader(5, "Attach plot context (base/current description + history) to each plot");
        logContext(map);

        logStepHeader(6, "Assign terrain difficulty and safety to each plot");
        logDifficultySafety(map);

        logStepHeader(7, "Add terrain features and smooth difficulty across neighboring plots");
        logFeaturesSmoothing(map);

        logStepHeader(8, "Seed flora (plant families and density per zone)");
        logFlora(map);

        logStepHeader(9, "Run deterministic Seed Golem world-sanity pass (walk, check, smooth)");
        GardenerWorldReport gardenerReport = GardenerWorldPass.run(map, GardenerWorldConfig.defaultConfig());
        logSanityReport(gardenerReport);

        IslandCreationReport creationReport = buildReport(map, gardenerReport);
        logReportSummary(creationReport);

        return new IslandCreationResult(map, creationReport);
    }

    private static void logStepHeader(int number, String name) {
        LOG.info("WORLD BUILD STEP {}: {}", number, name);
    }

    private static void logGeometry() {
        String envelope = "envelope x=[" + WorldGeometry.WORLD_MIN_X + "," + WorldGeometry.WORLD_MAX_X + "] "
                + "y=[" + WorldGeometry.WORLD_MIN_Y + "," + WorldGeometry.WORLD_MAX_Y + "]";
        String band = "islandBand x=[" + WorldGeometry.ISLAND_MIN_X + "," + WorldGeometry.ISLAND_MAX_X + "] "
                + "y=[" + WorldGeometry.ISLAND_MIN_Y + "," + WorldGeometry.ISLAND_MAX_Y + "]";
        String spawn = "spawn=" + WorldGeometry.SPAWN;
        LOG.debug("{}; {}; {}", envelope, band, spawn);
    }

    private static void logAnchors() {
        StringJoiner joiner = new StringJoiner(", ");
        for (AnchorTile tile : AnchorTiles.all()) {
            joiner.add(tile.getTileId() + "@" + tile.getPosition().x() + ":" + tile.getPosition().y());
        }
        LOG.debug("anchors count={} [{}]", AnchorTiles.all().size(), joiner);
    }

    private static void logAnchorTopology() {
        int links = 0;
        for (AnchorTile tile : AnchorTiles.all()) {
            for (Direction8 dir : Direction8.values()) {
                if (tile.getNeighbor(dir) != null) {
                    links++;
                }
            }
        }
        final int linkCount = links;
        final boolean reachable = allAnchorsReachable();
        LOG.debug("anchor neighbor links={}; all reachable from spawn={}", linkCount, reachable);
    }

    private static boolean allAnchorsReachable() {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(AnchorTiles.startTile().getTileId());
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            if (!visited.add(id)) continue;
            for (Direction8 dir : Direction8.values()) {
                String n = AnchorTiles.neighborOf(id, dir);
                if (n != null && !visited.contains(n)) {
                    queue.add(n);
                }
            }
        }
        return visited.size() == AnchorTiles.all().size();
    }

    private static void logGardenedMap(IslandMap map) {
        final int eligible = (WorldGeometry.ISLAND_MAX_X - WorldGeometry.ISLAND_MIN_X + 1)
                * (WorldGeometry.ISLAND_MAX_Y - WorldGeometry.ISLAND_MIN_Y + 1);
        int anchors = 0;
        int gardened = 0;
        int boundary = 0;
        for (IslandTile tile : map.allTiles()) {
            switch (tile.getKind()) {
                case ANCHOR -> anchors++;
                case GARDENED -> gardened++;
                case BOUNDARY -> boundary++;
            }
        }
        final boolean connected = isFullyConnected(map);
        final int total = map.allTiles().size();
        final int anchorsFinal = anchors;
        final int gardenedFinal = gardened;
        final int boundaryFinal = boundary;
        final boolean walkableConnected = connected;
        LOG.debug("plots total={} eligibleCoords={} anchors={} gardened={} boundary={}; walkable connected={}",
                total, eligible, anchorsFinal, gardenedFinal, boundaryFinal, walkableConnected);
    }

    private static boolean isFullyConnected(IslandMap map) {
        Set<Position> visited = new HashSet<>();
        Deque<Position> queue = new ArrayDeque<>();
        Position start = AnchorTiles.startTile().getPosition();
        queue.add(start);
        while (!queue.isEmpty()) {
            Position pos = queue.removeFirst();
            if (!visited.add(pos)) continue;
            IslandTile tile = map.get(pos).orElse(null);
            if (tile == null || !tile.isWalkable()) {
                continue;
            }
            for (Direction8 dir : Direction8.values()) {
                Position next = pos.step(dir);
                IslandTile maybe = map.get(next).orElse(null);
                if (maybe != null && maybe.isWalkable() && !visited.contains(next)) {
                    queue.add(next);
                }
            }
        }
        // only consider walkable tiles
        long walkableCount = map.allTiles().stream().filter(IslandTile::isWalkable).count();
        return visited.stream().filter(p -> map.get(p).map(IslandTile::isWalkable).orElse(false)).count() == walkableCount;
    }

    private static void logContext(IslandMap map) {
        final long withBase = map.allTiles().stream()
                .filter(t -> t.getContext() != null && !t.getContext().getBaseDescription().isEmpty())
                .count();
        final int total = map.allTiles().size();
        LOG.debug("plots with base/current descriptions={}/{}", withBase, total);
    }

    private static void logDifficultySafety(IslandMap map) {
        Map<TerrainDifficulty, Integer> diffCounts = new EnumMap<>(TerrainDifficulty.class);
        Map<TileSafety, Integer> safetyCounts = new EnumMap<>(TileSafety.class);
        for (TerrainDifficulty d : TerrainDifficulty.values()) diffCounts.put(d, 0);
        for (TileSafety s : TileSafety.values()) safetyCounts.put(s, 0);
        map.allTiles().forEach(t -> {
            diffCounts.put(t.getDifficulty(), diffCounts.get(t.getDifficulty()) + 1);
            safetyCounts.put(t.getSafety(), safetyCounts.get(t.getSafety()) + 1);
        });
        final Map<TerrainDifficulty, Integer> diffSnapshot = new EnumMap<>(diffCounts);
        final Map<TileSafety, Integer> safetySnapshot = new EnumMap<>(safetyCounts);
        LOG.debug("difficulty={}; safety={}", diffSnapshot, safetySnapshot);
    }

    private static void logFeaturesSmoothing(IslandMap map) {
        final long pathCount = map.allTiles().stream().filter(t -> t.getFeatures().contains(TerrainFeature.PATH)).count();
        final long cliffFaceCount = map.allTiles().stream().filter(t -> t.getFeatures().contains(TerrainFeature.CLIFF_FACE)).count();
        final long waterfallCount = map.allTiles().stream().filter(t -> t.getFeatures().contains(TerrainFeature.WATERFALL_DROP)).count();
        final boolean smoothingOk = smoothingConstraintHolds(map);
        LOG.debug("features PATH={} CLIFF_FACE={} WATERFALL_DROP={} ; smoothingConstraintOk={}",
                pathCount, cliffFaceCount, waterfallCount, smoothingOk);
    }

    private static boolean smoothingConstraintHolds(IslandMap map) {
        Set<TerrainFeature> abrupt = java.util.EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL);
        for (IslandTile tile : map.allTiles()) {
            if (tile.getSafety() != TileSafety.NORMAL || tile.getFeatures().stream().anyMatch(abrupt::contains)) {
                continue;
            }
            for (Direction8 dir : Direction8.values()) {
                IslandTile neighbor = map.get(tile.getPosition().step(dir)).orElse(null);
                if (neighbor == null) continue;
                if (neighbor.getSafety() != TileSafety.NORMAL || neighbor.getFeatures().stream().anyMatch(abrupt::contains)) {
                    continue;
                }
                int diff = Math.abs(idx(tile.getDifficulty()) - idx(neighbor.getDifficulty()));
                if (diff > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int idx(TerrainDifficulty d) {
        return switch (d) {
            case EASY -> 0;
            case NORMAL -> 1;
            case HARD -> 2;
            case EXTREME -> 3;
        };
    }

    private static void logFlora(IslandMap map) {
        Map<PlantFamily, Integer> primary = new EnumMap<>(PlantFamily.class);
        Map<PlantDensity, Integer> density = new EnumMap<>(PlantDensity.class);
        for (PlantFamily f : PlantFamily.values()) primary.put(f, 0);
        for (PlantDensity d : PlantDensity.values()) density.put(d, 0);
        map.allTiles().forEach(t -> {
            primary.put(t.getPrimaryPlantFamily(), primary.get(t.getPrimaryPlantFamily()) + 1);
            density.put(t.getPlantDensity(), density.get(t.getPlantDensity()) + 1);
        });
        LOG.debug("flora primary={}; density={}", primary, density);
    }

    private static void logSanityReport(GardenerWorldReport report) {
        LOG.debug("sanity plotsVisited={} anchorPlotsVisited={} difficultyOutliersFixed={} deadPlotsRelaxed={} impossiblePlotsRelaxed={} pathPlotsSmoothed={} warnings={}",
                report.getPlotsVisited(),
                report.getAnchorPlotsVisited(),
                report.getDifficultyOutliersFixed(),
                report.getDeadPlotsRelaxed(),
                report.getImpossiblePlotsRelaxed(),
                report.getPathPlotsSmoothed(),
                report.getWarnings());
    }

    private static IslandCreationReport buildReport(IslandMap map, GardenerWorldReport gardenerReport) {
        IslandCreationReport r = IslandCreationReport.empty();

        r.meta.worldId = "demo-island";
        r.meta.buildVersion = "v1-static";
        r.meta.gardenerSeed = GardenerWorldConfig.defaultConfig().getRandomSeed();

        r.geometry.worldMinX = WorldGeometry.WORLD_MIN_X;
        r.geometry.worldMaxX = WorldGeometry.WORLD_MAX_X;
        r.geometry.worldMinY = WorldGeometry.WORLD_MIN_Y;
        r.geometry.worldMaxY = WorldGeometry.WORLD_MAX_Y;
        r.geometry.islandMinX = WorldGeometry.ISLAND_MIN_X;
        r.geometry.islandMaxX = WorldGeometry.ISLAND_MAX_X;
        r.geometry.islandMinY = WorldGeometry.ISLAND_MIN_Y;
        r.geometry.islandMaxY = WorldGeometry.ISLAND_MAX_Y;
        r.geometry.islandEligiblePlotCount = (WorldGeometry.ISLAND_MAX_X - WorldGeometry.ISLAND_MIN_X + 1)
                * (WorldGeometry.ISLAND_MAX_Y - WorldGeometry.ISLAND_MIN_Y + 1);

        r.anchorPlotCount = AnchorTiles.all().size();
        r.spawnPlotId = AnchorTiles.startTile().getTileId();
        r.walkablePlotCount = (int) map.allTiles().stream().filter(t -> t.getSafety() == TileSafety.NORMAL && t.isWalkable()).count();
        r.exitCandidatePlotIds = AnchorTiles.all().stream().filter(AnchorTile::isExitCandidate).map(AnchorTile::getTileId).toList();
        r.anchors = AnchorTiles.all().stream().map(IslandWorldBuilder::toAnchorSummary).toList();

        r.connectivity = buildConnectivity(map, r.anchors);
        r.hazards = buildHazards(map, r.anchors);
        r.difficulty = buildDifficulty(map);
        r.features = buildFeatures(map);
        r.flora = buildFlora(map);
        r.gardener = buildGardener(gardenerReport);
        r.verdict = buildVerdict(r);

        return r;
    }

    private static IslandCreationReport.AnchorSummary toAnchorSummary(AnchorTile tile) {
        IslandCreationReport.AnchorSummary s = new IslandCreationReport.AnchorSummary();
        s.anchorId = tile.getTileId();
        s.position = tile.getPosition();
        s.biome = tile.getBiome();
        s.region = tile.getRegion();
        s.elevation = tile.getElevation();
        s.isSpawn = tile.isStartTile();
        s.isExitCandidate = tile.isExitCandidate();
        s.isHazard = tile.isHazard();
        s.isSecret = tile.isSecret();
        s.isHub = tile.isHub();
        return s;
    }

    private static IslandCreationReport.ConnectivitySummary buildConnectivity(IslandMap map, java.util.List<IslandCreationReport.AnchorSummary> anchors) {
        IslandCreationReport.ConnectivitySummary c = new IslandCreationReport.ConnectivitySummary();

        // reachable primes
        java.util.Set<String> reachableAnchors = reachableAnchors(map);
        c.allPrimePlotsReachableFromSpawn = reachableAnchors.containsAll(AnchorTiles.ids());
        c.unreachablePrimePlots = AnchorTiles.ids().stream().filter(id -> !reachableAnchors.contains(id)).toList();

        // reachable walkable
        java.util.Set<Position> reachableWalkable = reachableWalkable(map);
        c.walkablePlotsReachableFromSpawn = reachableWalkable.size();
        int totalWalkable = (int) map.allTiles().stream().filter(IslandTile::isWalkable).count();
        c.totalWalkablePlots = totalWalkable;
        c.unreachableWalkablePlots = totalWalkable - reachableWalkable.size();

        // distances and effort
        java.util.Map<String, Integer> steps = shortestSteps(map);
        java.util.Map<String, Integer> effort = easiestEffort(map);
        java.util.List<IslandCreationReport.AnchorSummary> enriched = new java.util.ArrayList<>();
        for (IslandCreationReport.AnchorSummary a : anchors) {
            IslandCreationReport.AnchorSummary copy = new IslandCreationReport.AnchorSummary();
            copy.anchorId = a.anchorId;
            copy.position = a.position;
            copy.biome = a.biome;
            copy.region = a.region;
            copy.elevation = a.elevation;
            copy.isSpawn = a.isSpawn;
            copy.isExitCandidate = a.isExitCandidate;
            copy.isHazard = a.isHazard;
            copy.isSecret = a.isSecret;
            copy.isHub = a.isHub;
            copy.shortestStepsFromSpawn = steps.getOrDefault(a.anchorId, Integer.MAX_VALUE);
            copy.minDifficultyScoreFromSpawn = effort.getOrDefault(a.anchorId, Integer.MAX_VALUE);
            enriched.add(copy);
        }
        c.anchorPaths = enriched;
        return c;
    }

    private static java.util.Set<String> reachableAnchors(IslandMap map) {
        java.util.Set<String> visitedIds = new java.util.HashSet<>();
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(AnchorTiles.startTile().getTileId());
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            if (!visitedIds.add(id)) continue;
            IslandTile tile = map.get(id).orElse(null);
            if (tile == null) continue;
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n != null && n.isWalkable()) {
                    queue.add(n.getTileId());
                }
            }
        }
        return visitedIds;
    }

    private static java.util.Set<Position> reachableWalkable(IslandMap map) {
        java.util.Set<Position> visited = new java.util.HashSet<>();
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
        queue.add(AnchorTiles.startTile().getPosition());
        while (!queue.isEmpty()) {
            Position pos = queue.removeFirst();
            if (!visited.add(pos)) continue;
            IslandTile tile = map.get(pos).orElse(null);
            if (tile == null || !tile.isWalkable()) continue;
            for (Direction8 dir : Direction8.values()) {
                Position next = pos.step(dir);
                IslandTile maybe = map.get(next).orElse(null);
                if (maybe != null && maybe.isWalkable() && !visited.contains(next)) {
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    private static java.util.Map<String, Integer> shortestSteps(IslandMap map) {
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        java.util.ArrayDeque<IslandTile> queue = new java.util.ArrayDeque<>();
        IslandTile start = map.get(AnchorTiles.startTile().getTileId()).orElseThrow();
        queue.add(start);
        dist.put(start.getTileId(), 0);
        while (!queue.isEmpty()) {
            IslandTile tile = queue.removeFirst();
            int d = dist.get(tile.getTileId());
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n == null || !n.isWalkable()) continue;
                if (!dist.containsKey(n.getTileId())) {
                    dist.put(n.getTileId(), d + 1);
                    queue.add(n);
                }
            }
        }
        return dist;
    }

    private static java.util.Map<String, Integer> easiestEffort(IslandMap map) {
        java.util.Map<String, Integer> cost = new java.util.HashMap<>();
        java.util.PriorityQueue<IslandTile> pq = new java.util.PriorityQueue<>(java.util.Comparator.comparingInt(t -> cost.getOrDefault(t.getTileId(), Integer.MAX_VALUE)));
        IslandTile start = map.get(AnchorTiles.startTile().getTileId()).orElseThrow();
        cost.put(start.getTileId(), 0);
        pq.add(start);
        while (!pq.isEmpty()) {
            IslandTile tile = pq.poll();
            int current = cost.get(tile.getTileId());
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n == null || !n.isWalkable()) continue;
                int nextCost = current + n.getDifficulty().getTimeCost();
                if (nextCost < cost.getOrDefault(n.getTileId(), Integer.MAX_VALUE)) {
                    cost.put(n.getTileId(), nextCost);
                    pq.add(n);
                }
            }
        }
        return cost;
    }

    private static IslandCreationReport.HazardSummary buildHazards(IslandMap map, java.util.List<IslandCreationReport.AnchorSummary> anchors) {
        IslandCreationReport.HazardSummary h = new IslandCreationReport.HazardSummary();
        h.normalPlotCount = (int) map.allTiles().stream().filter(t -> t.getSafety() == TileSafety.NORMAL).count();
        h.deadPlotCount = (int) map.allTiles().stream().filter(t -> t.getSafety() == TileSafety.DEAD).count();
        h.impossiblePlotCount = (int) map.allTiles().stream().filter(t -> t.getSafety() == TileSafety.IMPOSSIBLE).count();

        h.deadPlotsByRegion = map.allTiles().stream()
                .filter(t -> t.getSafety() == TileSafety.DEAD)
                .collect(java.util.stream.Collectors.groupingBy(IslandTile::getRegion, java.util.stream.Collectors.summingInt(t -> 1)));
        h.impossiblePlotsByRegion = map.allTiles().stream()
                .filter(t -> t.getSafety() == TileSafety.IMPOSSIBLE)
                .collect(java.util.stream.Collectors.groupingBy(IslandTile::getRegion, java.util.stream.Collectors.summingInt(t -> 1)));

        java.util.Map<String, Boolean> safePath = new java.util.HashMap<>();
        java.util.Map<String, Boolean> requiresHazard = new java.util.HashMap<>();
        java.util.Set<String> anchorIds = anchors.stream().map(a -> a.anchorId).collect(java.util.stream.Collectors.toSet());
        for (String id : anchorIds) {
            boolean hasSafe = hasSafePath(map, id);
            safePath.put(id, hasSafe);
            requiresHazard.put(id, !hasSafe);
        }
        h.anchorHasSafePath = safePath;
        h.anchorRequiresHazard = requiresHazard;
        return h;
    }

    private static boolean hasSafePath(IslandMap map, String targetId) {
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(AnchorTiles.startTile().getTileId());
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            if (!visited.add(id)) continue;
            if (id.equals(targetId)) return true;
            IslandTile tile = map.get(id).orElse(null);
            if (tile == null) continue;
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n == null || n.getSafety() != TileSafety.NORMAL) continue;
                queue.add(n.getTileId());
            }
        }
        return false;
    }

    private static IslandCreationReport.DifficultySummary buildDifficulty(IslandMap map) {
        IslandCreationReport.DifficultySummary d = new IslandCreationReport.DifficultySummary();
        java.util.Map<TerrainDifficulty, Integer> counts = new java.util.EnumMap<>(TerrainDifficulty.class);
        for (TerrainDifficulty td : TerrainDifficulty.values()) counts.put(td, 0);
        map.allTiles().forEach(t -> counts.put(t.getDifficulty(), counts.get(t.getDifficulty()) + 1));
        d.difficultyCounts = counts;

        java.util.Set<TerrainFeature> abrupt = java.util.EnumSet.of(TerrainFeature.CLIFF_FACE, TerrainFeature.WATERFALL_DROP, TerrainFeature.ROCK_WALL);
        int violations = 0;
        int maxDiff = 0;
        for (IslandTile tile : map.allTiles()) {
            if (tile.getFeatures().stream().anyMatch(abrupt::contains)) continue;
            for (Direction8 dir : Direction8.values()) {
                IslandTile n = map.get(tile.getPosition().step(dir)).orElse(null);
                if (n == null || n.getFeatures().stream().anyMatch(abrupt::contains)) continue;
                int delta = Math.abs(idx(tile.getDifficulty()) - idx(n.getDifficulty()));
                maxDiff = Math.max(maxDiff, delta);
                if (delta > 1) {
                    violations++;
                }
            }
        }
        d.neighborDifficultyViolations = violations;
        d.maxNeighborDifficultyDifference = maxDiff;
        return d;
    }

    private static IslandCreationReport.FeatureSummary buildFeatures(IslandMap map) {
        IslandCreationReport.FeatureSummary f = new IslandCreationReport.FeatureSummary();
        java.util.Map<TerrainFeature, Integer> counts = new java.util.EnumMap<>(TerrainFeature.class);
        for (TerrainFeature tf : TerrainFeature.values()) counts.put(tf, 0);
        map.allTiles().forEach(t -> t.getFeatures().forEach(feat -> counts.put(feat, counts.get(feat) + 1)));
        f.featureCounts = counts;

        java.util.List<Position> mainPath = java.util.List.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2),
                new Position(0, 3),
                new Position(0, 4)
        );
        boolean allPresent = mainPath.stream().allMatch(p -> map.get(p).isPresent());
        boolean allPathFeature = allPresent && mainPath.stream().allMatch(p -> map.get(p).get().getFeatures().contains(TerrainFeature.PATH));
        f.mainPathDefined = allPresent;
        f.mainPathAllPlotsHavePath = allPathFeature;
        f.mainPathContinuous = allPresent; // positions are ordered
        int steps = allPresent ? mainPath.size() - 1 : Integer.MAX_VALUE;
        f.mainPathTotalSteps = steps;
        int score = 0;
        if (allPresent) {
            // skip first (spawn) difficulty cost
            for (int i = 1; i < mainPath.size(); i++) {
                score += map.get(mainPath.get(i)).get().getDifficulty().getTimeCost();
            }
        } else {
            score = Integer.MAX_VALUE;
        }
        f.mainPathTotalDifficultyScore = score;
        return f;
    }

    private static IslandCreationReport.FloraSummary buildFlora(IslandMap map) {
        IslandCreationReport.FloraSummary f = new IslandCreationReport.FloraSummary();
        java.util.Map<PlantFamily, Integer> fam = new java.util.EnumMap<>(PlantFamily.class);
        java.util.Map<PlantDensity, Integer> density = new java.util.EnumMap<>(PlantDensity.class);
        for (PlantFamily pf : PlantFamily.values()) fam.put(pf, 0);
        for (PlantDensity pd : PlantDensity.values()) density.put(pd, 0);
        map.allTiles().forEach(t -> {
            fam.put(t.getPrimaryPlantFamily(), fam.get(t.getPrimaryPlantFamily()) + 1);
            density.put(t.getPlantDensity(), density.get(t.getPlantDensity()) + 1);
        });
        f.plantFamilyCounts = fam;
        f.plantDensityCounts = density;
        return f;
    }

    private static IslandCreationReport.GardenerSummary buildGardener(GardenerWorldReport gardenerReport) {
        IslandCreationReport.GardenerSummary g = new IslandCreationReport.GardenerSummary();
        g.plotsVisitedByGardener = gardenerReport.getPlotsVisited();
        g.anchorPlotsVisitedByGardener = gardenerReport.getAnchorPlotsVisited();
        g.walkablePlotsVisitedByGardener = gardenerReport.getWalkablePlotsVisited();
        g.unvisitedWalkablePlots = gardenerReport.getUnvisitedWalkablePlots();
        g.hasFullGardenerCoverage = gardenerReport.isHasFullCoverage();
        g.coveragePath = gardenerReport.getCoveragePath();
        g.difficultyOutliersFixedByGardener = gardenerReport.getDifficultyOutliersFixed();
        g.deadPlotsRelaxedByGardener = gardenerReport.getDeadPlotsRelaxed();
        g.impossiblePlotsRelaxedByGardener = gardenerReport.getImpossiblePlotsRelaxed();
        g.pathPlotsSmoothedByGardener = gardenerReport.getPathPlotsSmoothed();
        g.warnings = gardenerReport.getWarnings();
        return g;
    }

    private static IslandCreationReport.Verdict buildVerdict(IslandCreationReport r) {
        IslandCreationReport.Verdict v = new IslandCreationReport.Verdict();
        java.util.List<String> blocking = new java.util.ArrayList<>();
        java.util.List<String> warnings = new java.util.ArrayList<>();

        if (!r.connectivity.allPrimePlotsReachableFromSpawn) {
            blocking.add("Not all prime plots reachable: " + r.connectivity.unreachablePrimePlots);
        }
        if (r.connectivity.unreachableWalkablePlots > 0) {
            blocking.add("Unreachable walkable plots count=" + r.connectivity.unreachableWalkablePlots);
        }
        if (!r.gardener.hasFullGardenerCoverage) {
            blocking.add("Gardener did not reach all walkable plots; unvisited size=" + r.gardener.unvisitedWalkablePlots.size());
        }
        if (r.hazards.anchorRequiresHazard.values().stream().anyMatch(Boolean::booleanValue)) {
            warnings.add("Some anchors lack a safe path and may require hazards to cross.");
        }
        if (!r.gardener.warnings.isEmpty()) {
            warnings.addAll(r.gardener.warnings);
        }

        v.readyForCosmos = blocking.isEmpty();
        v.blockingIssues = blocking;
        v.nonBlockingWarnings = warnings;
        return v;
    }

    private static void logReportSummary(IslandCreationReport r) {
        // One INFO verdict line
        LOG.info("[REPORT] verdict readyForCosmos={} fullCoverage={}{}{}",
                r.verdict.readyForCosmos,
                r.gardener.hasFullGardenerCoverage,
                (r.verdict.blockingIssues.isEmpty() ? "" : " blocking=" + r.verdict.blockingIssues),
                (r.verdict.nonBlockingWarnings.isEmpty() ? "" : " warnings=" + r.verdict.nonBlockingWarnings));

        // Detailed report at DEBUG/ FINE
        LOG.debug("[REPORT] meta id={} build={} seed={}", r.meta.worldId, r.meta.buildVersion, r.meta.gardenerSeed);
        LOG.debug("[REPORT] geometry islandEligible={} anchors={}", r.geometry.islandEligiblePlotCount, r.anchorPlotCount);
        LOG.debug("[REPORT] connectivity primesReachable={} unreachablePrimes={} reachableWalkable={}/{}",
                r.connectivity.allPrimePlotsReachableFromSpawn,
                r.connectivity.unreachablePrimePlots,
                r.connectivity.walkablePlotsReachableFromSpawn,
                r.connectivity.totalWalkablePlots);
        LOG.debug("[REPORT] hazards dead={} impossible={}", r.hazards.deadPlotCount, r.hazards.impossiblePlotCount);
        LOG.debug("[REPORT] difficulty counts={} violations={} maxNeighborDelta={}",
                r.difficulty.difficultyCounts,
                r.difficulty.neighborDifficultyViolations,
                r.difficulty.maxNeighborDifficultyDifference);
        LOG.debug("[REPORT] features PATH={} mainPathContinuous={} mainPathScore={}",
                r.features.featureCounts.getOrDefault(TerrainFeature.PATH, 0),
                r.features.mainPathContinuous,
                r.features.mainPathTotalDifficultyScore);
        LOG.debug("[REPORT] flora families={}", r.flora.plantFamilyCounts);
        LOG.debug("[REPORT] gardener visited={} fixes(diff/ dead/ imp)={}/{}/{} walkableCoverage={}/{} fullCoverage={} unvisited={}",
                r.gardener.plotsVisitedByGardener,
                r.gardener.difficultyOutliersFixedByGardener,
                r.gardener.deadPlotsRelaxedByGardener,
                r.gardener.impossiblePlotsRelaxedByGardener,
                r.gardener.walkablePlotsVisitedByGardener,
                r.walkablePlotCount,
                r.gardener.hasFullGardenerCoverage,
                r.gardener.unvisitedWalkablePlots);
    }
}
