package com.demo.island.world;

/**
 * A single visit in the Gardener coverage path.
 */
public final class GardenerVisit {
    private final int index;
    private final String plotId;
    private final Position position;

    public GardenerVisit(int index, String plotId, Position position) {
        this.index = index;
        this.plotId = plotId;
        this.position = position;
    }

    public int getIndex() {
        return index;
    }

    public String getPlotId() {
        return plotId;
    }

    public Position getPosition() {
        return position;
    }
}
