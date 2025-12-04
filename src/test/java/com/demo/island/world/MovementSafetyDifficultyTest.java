package com.demo.island.world;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementSafetyDifficultyTest {

    @Test
    void hardTerrainCostsExtraTime() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        // Find a HARD tile and a neighbor to approach from.
        IslandTile hardTile = map.allTiles().stream()
                .filter(t -> t.getSafety() == TileSafety.NORMAL && t.getDifficulty() == TerrainDifficulty.HARD)
                .findFirst()
                .orElseThrow();

        Position hardPos = hardTile.getPosition();
        // pick a neighbor that exists and is normal
        Position fromPos = null;
        Direction8 dirUsed = null;
        for (Direction8 dir : Direction8.values()) {
            Position candidate = new Position(hardPos.x() - dir.dx(), hardPos.y() - dir.dy());
            if (map.get(candidate).map(IslandTile::getSafety).orElse(TileSafety.NORMAL) == TileSafety.NORMAL) {
                fromPos = candidate;
                dirUsed = dir;
                break;
            }
        }
        assertThat(fromPos).isNotNull();

        PlayerLocation start = new PlayerLocation(map.get(fromPos).orElseThrow().getTileId());
        MoveResult res = IslandMovement.move(map, start, dirUsed, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getTimeCost()).isEqualTo(hardTile.getDifficulty().getTimeCost());
        assertThat(res.getTimeCost()).isEqualTo(2);
    }

    @Test
    void extremeTerrainCostsThree() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        // Move from Stream into Waterfall Pool (EXTREME -> 3 turns)
        PlayerLocation stream = new PlayerLocation("T_STREAM");
        MoveResult res = IslandMovement.move(map, stream, Direction8.N, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_OK);
        assertThat(res.getTimeCost()).isEqualTo(3);
        assertThat(clock.getTurnIndex()).isEqualTo(3);
    }

    @Test
    void deadTileIsFatal() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        PlayerLocation loc = PlayerLocation.spawn();
        // Step east thrice to reach (3,0) which gardener marks as DEAD on shoreline corners
        loc = IslandMovement.move(map, loc, Direction8.E, clock).getLocation();
        loc = IslandMovement.move(map, loc, Direction8.E, clock).getLocation();
        MoveResult res = IslandMovement.move(map, loc, Direction8.E, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_FATAL_DEAD_TILE);
        assertThat(res.isGameOver()).isTrue();
        assertThat(res.getGameOverReason()).isEqualTo(GameOverReason.DEAD_TILE);
    }

    @Test
    void impossibleTileBlocksMovement() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        // Position (2,8) should be near rim; east to (3,8) marked IMPOSSIBLE
        PlayerLocation loc = new PlayerLocation("G_2_8");
        MoveResult res = IslandMovement.move(map, loc, Direction8.E, clock);
        assertThat(res.getOutcome()).isEqualTo(MoveOutcome.MOVE_BLOCKED_IMPOSSIBLE);
        assertThat(clock.getTurnIndex()).isZero();
    }

    @Test
    void runningOutOfTimeTriggersGameOver() {
        IslandMap map = IslandGardener.garden();
        TurnClock clock = new TurnClock();
        PlayerLocation loc = PlayerLocation.spawn();
        MoveResult res = null;
        // bounce east/west to accumulate time until out-of-time
        for (int i = 0; i < 40 && !clock.isOutOfTime(); i++) {
            Direction8 dir = (i % 2 == 0) ? Direction8.E : Direction8.W;
            res = IslandMovement.move(map, loc, dir, clock);
            loc = res.getLocation();
        }
        assertThat(clock.isOutOfTime()).isTrue();
        assertThat(res).isNotNull();
        assertThat(res.isGameOver()).isTrue();
        assertThat(res.getGameOverReason()).isEqualTo(GameOverReason.OUT_OF_TIME);
    }
}
