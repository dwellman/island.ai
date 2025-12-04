package com.demo.island.game;

import com.demo.island.world.IslandCreationResult;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandWorldBuilder;
import com.demo.island.world.PlayerLocation;
import com.demo.island.game.CosmosClock;

import java.util.EnumMap;
import java.util.Map;

/**
 * Game-layer session holding player state, clock, and world reference.
 */
public final class GameSession {

    private final IslandMap map;
    private final CosmosClock clock;
    private PlayerLocation location;
    private final Map<GameItemType, Integer> inventory = new EnumMap<>(GameItemType.class);
    private final Map<String, PlotResources> plotResources = new java.util.HashMap<>();
    private int raftProgress;
    private GameStatus status;
    private GameEndReason gameEndReason;

    public GameSession(IslandCreationResult creation) {
        this.map = creation.getMap();
        this.clock = new CosmosClock(CosmosClock.DEFAULT_MAX_PIPS);
        this.location = PlayerLocation.spawn();
        this.raftProgress = 0;
        this.status = GameStatus.IN_PROGRESS;
        this.gameEndReason = GameEndReason.NONE;
        seedResources();
    }

    private void seedResources() {
        // simple distribution; undiscovered until SEARCH
        putResource("T_WRECK_BEACH", r -> {
            r.add(GameItemType.METAL_SCRAP, 2);
            r.add(GameItemType.WOOD_LOG, 1);
        });
        putResource("T_TIDEPOOL_ROCKS", r -> r.add(GameItemType.METAL_SCRAP, 1));
        putResource("T_CAMP", r -> r.add(GameItemType.WOOD_LOG, 3));
        putResource("T_VINE_FOREST", r -> r.add(GameItemType.VINE_ROPE, 3));
    }

    private void putResource(String plotId, java.util.function.Consumer<PlotResources> seed) {
        PlotResources res = new PlotResources(false);
        seed.accept(res);
        plotResources.put(plotId, res);
    }

    public IslandMap getMap() {
        return map;
    }

    public CosmosClock getClock() {
        return clock;
    }

    public PlayerLocation getLocation() {
        return location;
    }

    public void setLocation(PlayerLocation location) {
        this.location = location;
    }

    public Map<GameItemType, Integer> getInventory() {
        return inventory;
    }

    public Map<String, PlotResources> getPlotResources() {
        return plotResources;
    }

    public int getRaftProgress() {
        return raftProgress;
    }

    public void incrementRaftProgress() {
        this.raftProgress += 1;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    public void setGameEndReason(GameEndReason reason) {
        this.gameEndReason = reason;
    }

    public boolean isRaftReady() {
        return raftProgress >= 3;
    }

    public static GameSession newSession() {
        IslandCreationResult creation = IslandWorldBuilder.buildWorldWithLogging();
        return new GameSession(creation);
    }
}
