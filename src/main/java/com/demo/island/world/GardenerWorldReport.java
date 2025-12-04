package com.demo.island.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary of the deterministic Gardener sanity pass.
 */
public final class GardenerWorldReport {
    private int plotsVisited;
    private int anchorPlotsVisited;
    private int walkablePlotsVisited;
    private int walkablePlotCount;
    private List<String> unvisitedWalkablePlots = List.of();
    private boolean hasFullCoverage;
    private List<GardenerVisit> coveragePath = List.of();
    private int difficultyOutliersFixed;
    private int deadPlotsRelaxed;
    private int impossiblePlotsRelaxed;
    private int pathPlotsSmoothed;
    private final List<String> warnings = new ArrayList<>();

    public int getPlotsVisited() {
        return plotsVisited;
    }

    public void incrementPlotsVisited() {
        this.plotsVisited++;
    }

    public int getAnchorPlotsVisited() {
        return anchorPlotsVisited;
    }

    public void incrementAnchorPlotsVisited() {
        this.anchorPlotsVisited++;
    }

    public int getWalkablePlotsVisited() {
        return walkablePlotsVisited;
    }

    public void setWalkablePlotsVisited(int walkablePlotsVisited) {
        this.walkablePlotsVisited = walkablePlotsVisited;
    }

    public int getWalkablePlotCount() {
        return walkablePlotCount;
    }

    public void setWalkablePlotCount(int walkablePlotCount) {
        this.walkablePlotCount = walkablePlotCount;
    }

    public List<String> getUnvisitedWalkablePlots() {
        return unvisitedWalkablePlots;
    }

    public void setUnvisitedWalkablePlots(List<String> unvisitedWalkablePlots) {
        this.unvisitedWalkablePlots = unvisitedWalkablePlots;
    }

    public boolean isHasFullCoverage() {
        return hasFullCoverage;
    }

    public void setHasFullCoverage(boolean hasFullCoverage) {
        this.hasFullCoverage = hasFullCoverage;
    }

    public List<GardenerVisit> getCoveragePath() {
        return coveragePath;
    }

    public void setCoveragePath(List<GardenerVisit> coveragePath) {
        this.coveragePath = coveragePath;
    }

    public int getDifficultyOutliersFixed() {
        return difficultyOutliersFixed;
    }

    public void incrementDifficultyOutliersFixed() {
        this.difficultyOutliersFixed++;
    }

    public int getDeadPlotsRelaxed() {
        return deadPlotsRelaxed;
    }

    public void incrementDeadPlotsRelaxed() {
        this.deadPlotsRelaxed++;
    }

    public int getImpossiblePlotsRelaxed() {
        return impossiblePlotsRelaxed;
    }

    public void incrementImpossiblePlotsRelaxed() {
        this.impossiblePlotsRelaxed++;
    }

    public int getPathPlotsSmoothed() {
        return pathPlotsSmoothed;
    }

    public void incrementPathPlotsSmoothed() {
        this.pathPlotsSmoothed++;
    }

    public List<String> getWarnings() {
        return java.util.Collections.unmodifiableList(warnings);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}
