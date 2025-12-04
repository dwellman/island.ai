package com.demo.island.world;

/**
 * Bundle of the finalized IslandMap plus its creation report.
 */
public final class IslandCreationResult {
    private final IslandMap map;
    private final IslandCreationReport report;

    public IslandCreationResult(IslandMap map, IslandCreationReport report) {
        this.map = map;
        this.report = report;
    }

    public IslandMap getMap() {
        return map;
    }

    public IslandCreationReport getReport() {
        return report;
    }
}
