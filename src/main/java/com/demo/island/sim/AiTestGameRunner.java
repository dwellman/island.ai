package com.demo.island.sim;

import com.demo.island.game.AgentDecision;
import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.game.GameEndReason;
import com.demo.island.game.GameEngine;
import com.demo.island.game.GameSession;
import com.demo.island.game.GameStatus;
import com.demo.island.game.PlayerToolEngine;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.PlayerToolResult;
import com.demo.island.config.AiConfig;
import com.demo.island.player.PlayerAgentPromptConfig;
import com.demo.island.player.PlayerAgentSpringConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.TileKind;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * External AI harness that plays the game and prints a test report.
 * Run via:
 * mvn -q exec:java -Dexec.mainClass=com.demo.island.sim.AiTestGameRunner
 */
public final class AiTestGameRunner {

    private AiTestGameRunner() {
    }

    public static void main(String[] args) {
        GameSession session = GameSession.newSession();
        ExternalPlayerAgent agent = resolveAgent();

        System.out.println(GameEngine.buildIntroMessage(session.getClock()));

        PlayerToolResult lastResult = null;
        int actions = 0;
        int actionCap = 2000;

        while (session.getStatus() == GameStatus.IN_PROGRESS && actions < actionCap) {
            AgentDecision decision = agent.decideNext(session, lastResult);
            PlayerToolRequest request = decision.getRequest();
            lastResult = new PlayerToolEngine(session).invoke(request);
            actions++;

            System.out.println("--- Turn " + actions + " ---");
            System.out.println("[Agent] Reason: " + decision.getReason());
            System.out.println(lastResult.getText());
        }

        printReport(session, agent, actions, actionCap, lastResult);
    }

    private static ExternalPlayerAgent resolveAgent() {
        boolean llmEnabled = Boolean.parseBoolean(System.getProperty("unna.player.llm.enabled", "false"));
        if (!llmEnabled) {
            return new SmartAiTestAgent();
        }
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(PlayerAgentPromptConfig.class, PlayerAgentSpringConfig.class, AiConfig.class);
            ctx.refresh();
            return ctx.getBean(ExternalPlayerAgent.class);
        } catch (Exception ex) {
            System.out.println("LLM agent not available, falling back to heuristic: " + ex.getMessage());
            return new SmartAiTestAgent();
        }
    }

    private static void printReport(GameSession session, ExternalPlayerAgent agent, int actions, int actionCap, PlayerToolResult lastResult) {
        GameEndReason reason = session.getGameEndReason();
        if (session.getStatus() == GameStatus.IN_PROGRESS && actions >= actionCap) {
            reason = GameEndReason.NONE;
        }

        IslandMap map = session.getMap();
        Set<String> anchorsAll = new HashSet<>();
        for (IslandTile tile : map.allTiles()) {
            if (tile.getKind() == TileKind.ANCHOR) {
                anchorsAll.add(tile.getTileId());
            }
        }
        Set<String> anchorsVisited = new HashSet<>();
        if (agent instanceof SmartAiTestAgent smart) {
            anchorsVisited.addAll(smart.getVisitedAnchors());
        }
        Set<String> anchorsNotVisited = new HashSet<>(anchorsAll);
        anchorsNotVisited.removeAll(anchorsVisited);

        System.out.println("\n=== AI TEST REPORT ===");
        System.out.println("Outcome: " + session.getStatus() + " (" + reasonOrCap(reason, actions, actionCap) + ")");
        System.out.println("Actions taken: " + actions);
        System.out.println("Final time: " + session.getClock().formatRemainingBracketed()
                + " elapsedHours=" + String.format(Locale.ROOT, "%.2f", session.getClock().getTotalPips() / 60.0));
        System.out.println("Final plot: " + session.getLocation().getTileId());
        System.out.println("Raft progress: " + session.getRaftProgress() + "/3 ready=" + session.isRaftReady());

        System.out.println("Anchors visited: " + anchorsVisited);
        System.out.println("Anchors not visited: " + anchorsNotVisited);
        if (agent instanceof SmartAiTestAgent smart) {
            System.out.println("Max resources seen: " + smart.getMaxInventorySeen());
        }

        System.out.println("Potential issues:");
        if (!anchorsNotVisited.isEmpty() && session.getClock().isOutOfTime()) {
            System.out.println("- Exploration issue: unvisited anchors " + anchorsNotVisited);
        }
        if (!session.isRaftReady() && session.getClock().isOutOfTime()) {
            System.out.println("- Resource/time issue: raft not ready before time ran out.");
        }
        if (reason == GameEndReason.NONE && session.getStatus() == GameStatus.IN_PROGRESS) {
            System.out.println("- Loop issue: hit action cap without finishing.");
        }
        if (lastResult != null && lastResult.getText() != null && lastResult.getText().isEmpty()) {
            System.out.println("- Messaging issue: last message was empty.");
        }
    }

    private static String reasonOrCap(GameEndReason reason, int actions, int cap) {
        if (reason == GameEndReason.NONE && actions >= cap) {
            return "CAP_REACHED";
        }
        return reason.toString();
    }
}
