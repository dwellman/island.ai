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
import com.demo.island.config.OpenAiClientConfig;
import com.demo.island.config.DmAgentPromptConfig;
import com.demo.island.config.GhostAgentPromptConfig;
import com.demo.island.config.MonkeyAgentPromptConfig;
import com.demo.island.game.PlayerTool;
import com.demo.island.player.PlayerAgentPromptConfig;
import com.demo.island.player.PlayerAgentSpringConfig;
import com.demo.island.player.LlmExternalPlayerAgent;
import com.demo.island.game.SpringAiDmAgent;
import com.demo.island.game.DmAgentConfig;
import com.demo.island.game.monkey.MonkeyAgent;
import com.demo.island.game.monkey.HeuristicMonkeyAgent;
import com.demo.island.game.monkey.MonkeyDecision;
import com.demo.island.game.monkey.MonkeyState;
import com.demo.island.game.monkey.SpringAiMonkeyAgent;
import com.demo.island.game.ghost.GhostAgentRegistry;
import com.demo.island.game.ghost.SpringAiGhostAgent;
import com.demo.island.world.CharacterThing;
import com.demo.island.game.ContextBuilder;
import com.demo.island.game.ToolActionExecutor;
import com.demo.island.game.ToolOutcome;
import com.demo.island.game.OutcomeType;
import com.demo.island.game.TurnContext;
import com.demo.island.game.TurnLogFormatter;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.TileKind;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * External AI harness that plays the game and prints a test report.
 * Run via:
 * mvn -q exec:java -Dexec.mainClass=com.demo.island.sim.AiTestGameRunner
 */
public final class AiTestGameRunner {

    private static final Logger LOG = LogManager.getLogger(AiTestGameRunner.class);

    private AiTestGameRunner() {
    }

    public static void main(String[] args) {
        GameSession session = GameSession.newSession();
        LOG.info("AiTestGameRunner: starting (Java {})", System.getProperty("java.version"));
        LOG.info("AiTestGameRunner: active profiles: default");
        GhostAgentRegistry.reset();
        AgentSelection selection = resolveAgent(args);
        ExternalPlayerAgent agent = selection.agent();
        boolean monkeyLlmEnabled = isFlagEnabled("monkey.agent.llm.enabled", args);
        MonkeyAgent monkeyAgent = monkeyLlmEnabled ? buildLlmMonkeyAgent() : null;
        if (monkeyAgent == null) {
            monkeyAgent = new HeuristicMonkeyAgent();
            LOG.info("Monkey Agent: heuristic (LLM disabled or prompt missing).");
        } else {
            LOG.info("Monkey Agent: Monkey1 LLM enabled.");
        }
        boolean ghostLlmEnabled = isFlagEnabled("ghost.agent.llm.enabled", args);
        if (ghostLlmEnabled) {
            var ghostAgent = buildLlmGhostAgent();
            if (ghostAgent != null) {
                GhostAgentRegistry.setAgent(ghostAgent);
                GhostAgentRegistry.setEnabledOverride(true);
                LOG.info("Ghost Agent: Ghost1 LLM enabled.");
            } else {
                GhostAgentRegistry.setEnabledOverride(false);
                LOG.info("Ghost Agent: prompt/client unavailable; using deterministic presence only.");
            }
        } else {
            GhostAgentRegistry.setEnabledOverride(false);
        }
        CharacterThing monkey = findMonkey(session);

        LOG.info(selection.logLine());

        LOG.info(GameEngine.buildIntroMessage(session.getClock()));

        PlayerToolResult lastResult = null;
        int actions = 0;
        int actionCap = 2000;

        while (session.getStatus() == GameStatus.IN_PROGRESS && actions < actionCap) {
            AgentDecision decision = agent.decideNext(session, lastResult);
            PlayerToolRequest request = decision.getRequest();
            lastResult = new PlayerToolEngine(session).invoke(request);
            actions++;

            logToolExecution(session, decision);
            LOG.info(formatTurnLog(session, null, "[Agent] Reason: " + decision.getReason()
                    + " Mood=" + decision.getMood()
                    + (decision.getNote() == null || decision.getNote().isBlank() ? "" : " Note=" + decision.getNote())));
            LOG.info(lastResult.getText());
            TurnContext turnCtx = lastResult.getTurnContext();
            if (turnCtx != null && turnCtx.ghostEventTriggered) {
                LOG.info(formatTurnLog(session, turnCtx.ghostEventPlotId, ghostLog(turnCtx)));
            }

            if (monkey != null) {
                runMonkeyTurn(session, monkeyAgent, monkey, actions);
            }
        }

        printReport(session, agent, actions, actionCap, lastResult);
        if (selection.context() != null) {
            selection.context().close();
        }
    }

    private static AgentSelection resolveAgent(String[] args) {
        boolean llmEnabled = isFlagEnabled("unna.player.llm.enabled", args)
                || isFlagEnabled("player.ai", args); // convenience alias for enabling AI runs
        boolean dmAgentEnabled = isFlagEnabled("dm.agent.enabled", args);
        if (dmAgentEnabled) {
            System.setProperty("dm.agent.enabled", "true");
        }
        if (!llmEnabled) {
            return new AgentSelection(new SmartAiTestAgent(),
                    "ExternalPlayerAgent: HEURISTIC (LLM disabled)",
                    null);
        }

        boolean apiKeyPresent = hasText(resolveArgValue(args, "spring.ai.openai.api-key"))
                || hasText(System.getenv("SPRING_AI_OPENAI_API_KEY"))
                || hasText(System.getenv("OPENAI_API_KEY"))
                || hasText(System.getProperty("spring.ai.openai.api-key"));
        if (!apiKeyPresent) {
            throw new IllegalStateException("LLM enabled but no OpenAI API key configured. Provide spring.ai.openai.api-key or SPRING_AI_OPENAI_API_KEY.");
        }

        try {
            // Force-enable Spring AI OpenAI support regardless of defaults in application.properties.
            System.setProperty("spring.ai.openai.enabled", "true");
            System.setProperty("SPRING_AI_OPENAI_ENABLED", "true");

            ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
                    .sources(PlayerAgentPromptConfig.class, PlayerAgentSpringConfig.class, AiConfig.class, OpenAiClientConfig.class, DmAgentPromptConfig.class)
                    .bannerMode(Banner.Mode.OFF)
                    .web(WebApplicationType.NONE)
                    .properties("spring.ai.openai.enabled=true", "SPRING_AI_OPENAI_ENABLED=true")
                    .run(args);

            String aiEnabledProp = ctx.getEnvironment().getProperty("spring.ai.openai.enabled");
            boolean hasChatClient = !ctx.getBeansOfType(org.springframework.ai.chat.client.ChatClient.class).isEmpty();
            LOG.info("AI flags: spring.ai.openai.enabled={} chatClientPresent={}", aiEnabledProp, hasChatClient);
            if (!hasChatClient) {
                ctx.close();
                throw new IllegalStateException("LLM enabled but ChatClient was not created (check spring.ai.openai configuration).");
            }

            org.springframework.ai.chat.client.ChatClient chatClient = ctx.getBean(org.springframework.ai.chat.client.ChatClient.class);
            String systemPrompt = ctx.getBean("playerAgentSystemPrompt", String.class);
            java.util.Map<PlayerTool, com.demo.island.player.ToolPrompt> toolPrompts = ctx.getBean("playerToolPrompts", java.util.Map.class);
            if (dmAgentEnabled) {
                try {
                    String dmPrompt = ctx.getBean("dmAgentSystemPrompt", String.class);
                    if (dmPrompt == null || dmPrompt.isBlank()) {
                        LOG.info("DM Agent: prompt not available; using core DM text.");
                        PlayerToolEngine.resetDmAgentForTests();
                        DmAgentConfig.setEnabledOverride(false);
                    } else {
                        PlayerToolEngine.setDmAgentForTests(new SpringAiDmAgent(chatClient, dmPrompt));
                        LOG.info("DM Agent: DM1 enabled via Spring AI.");
                    }
                } catch (Exception ex) {
                    LOG.info("DM Agent: failed to initialize; using core DM text. err={}", ex.getMessage());
                    PlayerToolEngine.resetDmAgentForTests();
                    DmAgentConfig.setEnabledOverride(false);
                }
            } else {
                PlayerToolEngine.resetDmAgentForTests();
            }

            LlmExternalPlayerAgent llmAgent = ctx.getBeanProvider(LlmExternalPlayerAgent.class).getIfAvailable();
            if (llmAgent == null) {
                llmAgent = new LlmExternalPlayerAgent(chatClient, systemPrompt, toolPrompts);
            }

            return new AgentSelection(llmAgent,
                    "ExternalPlayerAgent: LLM (Spring AI tool-calling active)",
                    ctx);
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException) {
                throw ex;
            }
            throw new IllegalStateException("LLM agent failed to initialize safely.", ex);
        }
    }

    private static void runMonkeyTurn(GameSession session, MonkeyAgent monkeyAgent, CharacterThing monkey, int turnNumber) {
        String plotId = monkey.getCurrentPlotId();
        var plotCtx = ContextBuilder.buildPlotContext(session, plotId);
        MonkeyState state = new MonkeyState(
                turnNumber,
                monkey.getId(),
                plotId,
                plotCtx.plotId,
                plotCtx.currentDescription,
                plotCtx.neighborSummaries == null ? Map.of() : plotCtx.neighborSummaries.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)),
                session.getLocation().getTileId().equals(plotId)
        );
        MonkeyDecision decision = monkeyAgent.decide(state);
        if (decision == null || decision.request() == null) {
            return;
        }
        ToolActionExecutor executor = new ToolActionExecutor();
        var ctx = ToolActionExecutor.buildContext(session, plotCtx, decision.request(), decision.targetRaw(), decision.reason(), decision.mood().name(), "", monkey.getId());
        ToolOutcome outcome = executor.execute(ctx);
        PlayerToolRequest req = decision.request();
        StringBuilder sb = new StringBuilder();
        sb.append("MonkeyToolEngine: ").append(req.getTool());
        if (req.getDirection() != null) {
            sb.append(" direction=").append(req.getDirection());
        }
        sb.append(" mood=").append(decision.mood());
        if (decision.reason() != null && !decision.reason().isBlank()) {
            String clean = decision.reason().replaceAll("\\s+", " ").trim();
            if (clean.length() > 80) {
                clean = clean.substring(0, 80) + "...";
            }
            sb.append(" reason=\"").append(clean).append("\"");
        }
        LOG.info(formatTurnLog(session, monkey.getCurrentPlotId(), sb.toString()));
        LOG.info(formatTurnLog(session, monkey.getCurrentPlotId(),
                "[Monkey] Outcome: " + (outcome.getOutcomeType() == OutcomeType.SUCCESS ? "success" : "blocked")
                        + " reasonCode=" + outcome.getReasonCode()));
    }

    private static CharacterThing findMonkey(GameSession session) {
        return session.getThingIndex().getAll().values().stream()
                .filter(t -> t instanceof CharacterThing ct && ct.getTags().contains("MONKEY_TROOP"))
                .map(t -> (CharacterThing) t)
                .findFirst()
                .orElse(null);
    }

    private static MonkeyAgent buildLlmMonkeyAgent() {
        try {
            ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
                    .sources(MonkeyAgentPromptConfig.class, OpenAiClientConfig.class)
                    .bannerMode(Banner.Mode.OFF)
                    .web(WebApplicationType.NONE)
                    .properties("spring.ai.openai.enabled=true", "SPRING_AI_OPENAI_ENABLED=true")
                    .run();
            String prompt = ctx.getBean("monkeyAgentSystemPrompt", String.class);
            if (prompt == null || prompt.isBlank()) {
                ctx.close();
                return null;
            }
            org.springframework.ai.chat.client.ChatClient chatClient = ctx.getBean(org.springframework.ai.chat.client.ChatClient.class);
            return new SpringAiMonkeyAgent(chatClient, prompt);
        } catch (Exception ex) {
            return null;
        }
    }

    private static SpringAiGhostAgent buildLlmGhostAgent() {
        try {
            ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
                    .sources(GhostAgentPromptConfig.class, OpenAiClientConfig.class)
                    .bannerMode(Banner.Mode.OFF)
                    .web(WebApplicationType.NONE)
                    .properties("spring.ai.openai.enabled=true", "SPRING_AI_OPENAI_ENABLED=true")
                    .run();
            String prompt = ctx.getBean("ghostAgentSystemPrompt", String.class);
            if (prompt == null || prompt.isBlank()) {
                ctx.close();
                return null;
            }
            org.springframework.ai.chat.client.ChatClient chatClient = ctx.getBean(org.springframework.ai.chat.client.ChatClient.class);
            return new SpringAiGhostAgent(chatClient, prompt);
        } catch (Exception ex) {
            return null;
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

        LOG.info("\n=== AI TEST REPORT ===");
        LOG.info("Outcome: {} ({})", session.getStatus(), reasonOrCap(reason, actions, actionCap));
        LOG.info("Actions taken: {}", actions);
        LOG.info("Final time: {} elapsedHours={}",
                session.getClock().formatRemainingBracketed(),
                String.format(Locale.ROOT, "%.2f", session.getClock().getTotalPips() / 60.0));
        LOG.info("Final plot: {}", session.getLocation().getTileId());
        LOG.info("Raft progress: {}/3 ready={}", session.getRaftProgress(), session.isRaftReady());

        LOG.info("Anchors visited: {}", anchorsVisited);
        LOG.info("Anchors not visited: {}", anchorsNotVisited);
        if (agent instanceof SmartAiTestAgent smart) {
            LOG.info("Max resources seen: {}", smart.getMaxInventorySeen());
        }

        LOG.info("Potential issues:");
        if (!anchorsNotVisited.isEmpty() && session.getClock().isOutOfTime()) {
            LOG.info("- Exploration issue: unvisited anchors {}", anchorsNotVisited);
        }
        if (!session.isRaftReady() && session.getClock().isOutOfTime()) {
            LOG.info("- Resource/time issue: raft not ready before time ran out.");
        }
        if (reason == GameEndReason.NONE && session.getStatus() == GameStatus.IN_PROGRESS) {
            LOG.info("- Loop issue: hit action cap without finishing.");
        }
        if (lastResult != null && lastResult.getText() != null && lastResult.getText().isEmpty()) {
            LOG.info("- Messaging issue: last message was empty.");
        }
    }

    private static void logToolExecution(GameSession session, AgentDecision decision) {
        PlayerToolRequest req = decision.getRequest();
        StringBuilder sb = new StringBuilder();
        boolean fallbackLook = req.getTool() == PlayerTool.LOOK
                && decision.getReason() != null
                && decision.getReason().startsWith("Fallback");
        sb.append("PlayerToolEngine: ");
        sb.append(fallbackLook ? "FALLBACK_LOOK" : req.getTool());
        if (req.getDirection() != null) {
            sb.append(" direction=").append(req.getDirection());
        }
        if (req.getItemType() != null) {
            sb.append(" item=").append(req.getItemType());
        }
        sb.append(" mood=").append(decision.getMood());
        String reason = decision.getReason() == null ? "" : decision.getReason().replaceAll("\\s+", " ").trim();
        if (!reason.isEmpty()) {
            if (reason.length() > 80) {
                reason = reason.substring(0, 80) + "...";
            }
            sb.append(" reason=\"").append(reason).append("\"");
        }
        String note = decision.getNote();
        if (note != null && !note.isBlank()) {
            String cleaned = note.replaceAll("\\s+", " ").trim();
            if (cleaned.length() > 60) {
                cleaned = cleaned.substring(0, 60) + "...";
            }
            sb.append(" note=\"").append(cleaned).append("\"");
        }
        LOG.info(formatTurnLog(session, null, sb.toString()));
    }

    private static String ghostLog(TurnContext turnCtx) {
        StringBuilder sb = new StringBuilder("GhostEvent: ghost presence");
        if (turnCtx.ghostEventPlotId != null) {
            sb.append(" at plot=").append(turnCtx.ghostEventPlotId);
        }
        if (turnCtx.ghostEventReason != null && !turnCtx.ghostEventReason.isBlank()) {
            sb.append(" reason=").append(turnCtx.ghostEventReason);
        }
        if (turnCtx.ghostMode != null && !turnCtx.ghostMode.isBlank()) {
            sb.append(" mode=").append(turnCtx.ghostMode);
        }
        if (turnCtx.ghostEventText != null && !turnCtx.ghostEventText.isBlank()) {
            String clean = turnCtx.ghostEventText.replaceAll("\\s+", " ").trim();
            if (clean.length() > 120) {
                clean = clean.substring(0, 120) + "...";
            }
            sb.append(" detail=\"").append(clean).append("\"");
        }
        if (turnCtx.ghostText != null && !turnCtx.ghostText.isBlank()) {
            String clean = turnCtx.ghostText.replaceAll("\\s+", " ").trim();
            if (clean.length() > 120) {
                clean = clean.substring(0, 120) + "...";
            }
            sb.append(" whisper=\"").append(clean).append("\"");
        }
        return sb.toString();
    }

    private static String reasonOrCap(GameEndReason reason, int actions, int cap) {
        if (reason == GameEndReason.NONE && actions >= cap) {
            return "CAP_REACHED";
        }
        return reason.toString();
    }

    private static boolean isFlagEnabled(String key, String[] args) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return Boolean.parseBoolean(sysProp);
        }
        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return Boolean.parseBoolean(envVal);
        }
        String argVal = resolveArgValue(args, key);
        if (argVal != null) {
            return Boolean.parseBoolean(argVal);
        }
        return false;
    }

    private static String resolveArgValue(String[] args, String key) {
        if (args == null || args.length == 0) {
            return null;
        }
        String withPrefix = "--" + key + "=";
        String plain = key + "=";
        for (String arg : args) {
            if (arg.startsWith(withPrefix)) {
                return arg.substring(withPrefix.length());
            }
            if (arg.startsWith(plain)) {
                return arg.substring(plain.length());
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static String formatTurnLog(GameSession session, String plotIdOverride, String message) {
        return TurnLogFormatter.format(session, plotIdOverride, message);
    }

    private record AgentSelection(ExternalPlayerAgent agent, String logLine, ConfigurableApplicationContext context) {
    }
}
