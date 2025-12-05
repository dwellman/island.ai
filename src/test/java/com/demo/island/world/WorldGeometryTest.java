package com.demo.island.world;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorldGeometryTest {

    @Test
    void directionDeltasAreCorrect() {
        Position origin = new Position(0, 0);
        assertThat(origin.step(Direction8.N)).isEqualTo(new Position(0, 1));
        assertThat(origin.step(Direction8.NE)).isEqualTo(new Position(1, 1));
        assertThat(origin.step(Direction8.E)).isEqualTo(new Position(1, 0));
        assertThat(origin.step(Direction8.SE)).isEqualTo(new Position(1, -1));
        assertThat(origin.step(Direction8.S)).isEqualTo(new Position(0, -1));
        assertThat(origin.step(Direction8.SW)).isEqualTo(new Position(-1, -1));
        assertThat(origin.step(Direction8.W)).isEqualTo(new Position(-1, 0));
        assertThat(origin.step(Direction8.NW)).isEqualTo(new Position(-1, 1));
    }

    @Test
    void envelopeClassification() {
        assertThat(WorldGeometry.isInsideWorldEnvelope(new Position(0, 0))).isTrue();
        assertThat(WorldGeometry.isInsideWorldEnvelope(new Position(-5, -1))).isTrue();
        assertThat(WorldGeometry.isInsideWorldEnvelope(new Position(6, 9))).isTrue();
        assertThat(WorldGeometry.isInsideWorldEnvelope(new Position(-6, 0))).isFalse();
        assertThat(WorldGeometry.isInsideWorldEnvelope(new Position(0, 10))).isFalse();
    }

    @Test
    void islandBandClassification() {
        assertThat(WorldGeometry.classify(new Position(0, 0))).isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE);
        assertThat(WorldGeometry.classify(new Position(4, 8))).isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE);
        assertThat(WorldGeometry.classify(new Position(5, 0))).isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE);
        assertThat(WorldGeometry.classify(new Position(6, 0))).isEqualTo(WorldGeometry.Classification.BOUNDARY);
        assertThat(WorldGeometry.classify(new Position(0, -1))).isEqualTo(WorldGeometry.Classification.BOUNDARY);
        assertThat(WorldGeometry.classify(new Position(0, 9))).isEqualTo(WorldGeometry.Classification.BOUNDARY);
    }

    @Test
    void spawnInvariant() {
        assertThat(WorldGeometry.SPAWN).isEqualTo(new Position(0, 0));
        assertThat(WorldGeometry.classify(WorldGeometry.SPAWN)).isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE);
    }

    @Test
    void movementAndBoundsNorthward() {
        Position pos = WorldGeometry.SPAWN;
        for (int i = 0; i < 8; i++) {
            pos = WorldGeometry.apply(pos, Direction8.N);
            assertThat(WorldGeometry.classify(pos)).isEqualTo(WorldGeometry.Classification.ISLAND_ELIGIBLE);
        }
        // Now at (0,8); next step to (0,9) is boundary
        pos = WorldGeometry.apply(pos, Direction8.N);
        assertThat(pos).isEqualTo(new Position(0, 9));
        assertThat(WorldGeometry.classify(pos)).isEqualTo(WorldGeometry.Classification.BOUNDARY);
        // Next step off-world
        pos = WorldGeometry.apply(pos, Direction8.N);
        assertThat(pos).isEqualTo(new Position(0, 10));
        assertThat(WorldGeometry.classify(pos)).isEqualTo(WorldGeometry.Classification.OFF_WORLD);
    }

    @Test
    void eastFromRightEdgeIsOffWorld() {
        Position edge = new Position(6, 5);
        assertThat(WorldGeometry.classify(edge)).isEqualTo(WorldGeometry.Classification.BOUNDARY);
        assertThat(WorldGeometry.classify(WorldGeometry.apply(edge, Direction8.E))).isEqualTo(WorldGeometry.Classification.OFF_WORLD);
        assertThat(WorldGeometry.classify(WorldGeometry.apply(edge, Direction8.NE))).isEqualTo(WorldGeometry.Classification.OFF_WORLD);
        assertThat(WorldGeometry.classify(WorldGeometry.apply(edge, Direction8.SE))).isEqualTo(WorldGeometry.Classification.OFF_WORLD);
    }
}
