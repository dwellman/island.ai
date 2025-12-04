package com.demo.island.game;

import java.util.List;
import java.util.Map;

/**
 * Lightweight snapshot for external PlayerAgents.
 */
public final class PlayerToolState {
    public String time;
    public String phase;

    public String locationId;
    public String locationSummary;
    public List<String> visibleItems;
    public Map<String, String> visibleExits;

    public List<String> inventory;
    public int raftProgress;
    public boolean raftReady;

    public PlayerTool lastTool;
    public String lastToolResult;
}
