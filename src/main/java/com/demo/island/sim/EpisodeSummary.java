package com.demo.island.sim;

import com.demo.island.game.GameEndReason;
import com.demo.island.game.GameStatus;
import com.demo.island.game.PlayerTool;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EpisodeSummary {
    public int turnsUsed;
    public GameStatus gameStatus;
    public GameEndReason gameEndReason;
    public String finalTime;
    public String finalLocationId;
    public Set<String> anchorsVisited;
    public int maxRaftProgress;
    public Map<String, Integer> maxResourcesSeen;
    public List<PlayerTool> toolHistory;
    public List<AgentDecisionLog> decisionLog;
}
