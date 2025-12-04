package com.demo.island.sim;

import com.demo.island.game.AgentDecision;
import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.game.GameActionResult;
import com.demo.island.game.GameEndReason;
import com.demo.island.game.GameSession;
import com.demo.island.game.GameStatus;
import com.demo.island.game.PlayerTool;
import com.demo.island.game.PlayerToolEngine;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.PlayerToolResult;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.TileKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs a bounded tool-based episode for an external player agent.
 */
public final class ToolEpisodeRunner {

    public EpisodeSummary runEpisode(ExternalPlayerAgent agent, ToolEpisodeConfig config) {
        GameSession session = GameSession.newSession();
        PlayerToolEngine toolEngine = new PlayerToolEngine(session);

        PlayerToolResult lastResult = new PlayerToolResult(session.getClock().formatRemainingBracketed()
                + " You are standing in the dark just before dawn. You have no idea how you got here.",
                null);

        int turnsUsed = 0;
        int maxTurns = config.getMaxTurns();
        Set<String> anchorsVisited = new HashSet<>();
        List<PlayerTool> toolHistory = new ArrayList<>();
        List<AgentDecisionLog> decisionLogs = new ArrayList<>();
        Map<String, Integer> maxResources = new HashMap<>();
        int maxRaftProgress = 0;

        while (session.getStatus() == GameStatus.IN_PROGRESS && turnsUsed < maxTurns) {
            AgentDecision decision = agent.decideNext(session, lastResult);
            PlayerToolRequest request = decision.getRequest();
            PlayerToolResult result = toolEngine.invoke(request);

            toolHistory.add(request.getTool());
            turnsUsed++;
            lastResult = result;
            decisionLogs.add(AgentDecisionLog.from(turnsUsed, session.getClock().formatRemainingBracketed(), decision));

            // track anchors
            IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElseThrow();
            if (current.getKind() == TileKind.ANCHOR) {
                anchorsVisited.add(current.getTileId());
            }
            // track resources
            session.getInventory().forEach((k, v) -> maxResources.put(k.name(), Math.max(maxResources.getOrDefault(k.name(), 0), v)));
            maxRaftProgress = Math.max(maxRaftProgress, session.getRaftProgress());
        }

        EpisodeSummary summary = new EpisodeSummary();
        summary.turnsUsed = turnsUsed;
        summary.gameStatus = session.getStatus();
        summary.gameEndReason = session.getGameEndReason();
        summary.finalTime = session.getClock().formatRemainingBracketed();
        summary.finalLocationId = session.getLocation().getTileId();
        summary.anchorsVisited = anchorsVisited;
        summary.maxRaftProgress = maxRaftProgress;
        summary.maxResourcesSeen = maxResources;
        summary.toolHistory = toolHistory;
        summary.decisionLog = decisionLogs;
        if (summary.gameEndReason == null) {
            summary.gameEndReason = GameEndReason.NONE;
        }
        return summary;
    }
}
