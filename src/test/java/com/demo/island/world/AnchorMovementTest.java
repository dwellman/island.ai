package com.demo.island.world;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorMovementTest {

    @Test
    void spawnAtWreckBeach() {
        PlayerLocation loc = PlayerLocation.spawn();
        IslandMap map = IslandGardener.garden();
        assertThat(loc.getTileId()).isEqualTo("T_WRECK_BEACH");
        assertThat(loc.getPosition(map)).isEqualTo(new Position(0, 0));
    }

    @Test
    void reachabilitySequences() {
        // N -> Camp
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        PlayerLocation loc = PlayerLocation.spawn();
        MoveResult res = IslandMovement.move(map, loc, Direction8.N, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_CAMP");

        // N, N -> Bamboo Grove
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Camp
        res = IslandMovement.move(map, loc, Direction8.N, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_BAMBOO_GROVE");

        // N, N, N -> Cliff Edge
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Camp
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Bamboo
        res = IslandMovement.move(map, loc, Direction8.N, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_CLIFF_EDGE");

        // N, N, N, N -> Signal Hill
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Camp
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Bamboo
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Cliff
        res = IslandMovement.move(map, loc, Direction8.N, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_SIGNAL_HILL");

        // NE -> Stream
        loc = PlayerLocation.spawn();
        res = IslandMovement.move(map, loc, Direction8.NE, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_STREAM");

        // E -> Tidepool Rocks
        loc = PlayerLocation.spawn();
        res = IslandMovement.move(map, loc, Direction8.E, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_TIDEPOOL_ROCKS");

        // W -> Hidden Cove
        loc = PlayerLocation.spawn();
        res = IslandMovement.move(map, loc, Direction8.W, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_HIDDEN_COVE");

        // NW -> Vine Forest
        loc = PlayerLocation.spawn();
        res = IslandMovement.move(map, loc, Direction8.NW, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_VINE_FOREST");

        // N, NE -> Waterfall Pool
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Camp
        res = IslandMovement.move(map, loc, Direction8.NE, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_WATERFALL_POOL");

        // NE, NE -> Cave Entrance
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.NE, clock).getLocation(); // Stream
        res = IslandMovement.move(map, loc, Direction8.NE, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_CAVE_ENTRANCE");

        // N, NW -> Old Ruins
        loc = PlayerLocation.spawn();
        loc = IslandMovement.move(map, loc, Direction8.N, clock).getLocation(); // Camp
        res = IslandMovement.move(map, loc, Direction8.NW, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_OLD_RUINS");
    }

    @Test
    void failedMovementFromEdges() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        PlayerLocation spawn = PlayerLocation.spawn();
        assertBlocked(IslandMovement.move(map, spawn, Direction8.S, clock));
        assertBlocked(IslandMovement.move(map, spawn, Direction8.SE, clock));
        assertBlocked(IslandMovement.move(map, spawn, Direction8.SW, clock));

        // From Signal Hill: N blocked, S ok to Cliff Edge
        PlayerLocation hill = new PlayerLocation("T_SIGNAL_HILL");
        MoveResult res = IslandMovement.move(map, hill, Direction8.N, clock);
        assertBlocked(res);
        res = IslandMovement.move(map, hill, Direction8.S, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getLocation().getTileId()).isEqualTo("T_CLIFF_EDGE");
    }

    @Test
    void noGhostTilesOnAnyMove() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        for (AnchorTile tile : AnchorTiles.all()) {
            PlayerLocation loc = new PlayerLocation(tile.getTileId());
            for (Direction8 dir : Direction8.values()) {
                MoveResult res = IslandMovement.move(map, loc, dir, clock);
                if (res.getOutcome() == MoveOutcome.MOVE_OK) {
                    assertThat(map.get(res.getLocation().getTileId())).isPresent();
                } else {
                    assertThat(res.getLocation().getTileId()).isEqualTo(tile.getTileId());
                }
            }
        }
    }

    private void assertBlocked(MoveResult result) {
        assertThat(result.getOutcome()).isIn(
                MoveOutcome.MOVE_BLOCKED_NO_TILE,
                MoveOutcome.MOVE_BLOCKED_OFF_WORLD_OR_BOUNDARY,
                MoveOutcome.MOVE_BLOCKED_IMPOSSIBLE);
    }
}
