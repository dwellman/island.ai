package com.demo.island.sim;

import com.demo.island.config.AiConfig;
import com.demo.island.config.OpenAiClientConfig;
import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.game.GameSession;
import com.demo.island.game.PlayerToolEngine;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.memory.MemorySummary;
import com.demo.island.game.memory.MemorySummaryBuilder;
import com.demo.island.player.LlmExternalPlayerAgent;
import com.demo.island.player.PlayerAgentPromptConfig;
import com.demo.island.player.PlayerAgentSpringConfig;
import com.demo.island.world.Direction8;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Small harness that walks a short path, builds memory, and asks the agent to explain it.
 * Run via:
 * mvn -q exec:java -Dexec.mainClass=com.demo.island.sim.MemoryExplainRunner -Dunna.player.llm.enabled=true
 */
public final class MemoryExplainRunner {

    private static final Logger LOG = LogManager.getLogger(MemoryExplainRunner.class);

    private MemoryExplainRunner() {
    }

    public static void main(String[] args) {
        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);
        LOG.info("MemoryExplainRunner: starting probe (Java {})", System.getProperty("java.version"));

        runScriptedPath(engine);
        MemorySummary summary = MemorySummaryBuilder.build(session);
        LOG.info("Memory summary after scripted walk:\n{}", summary.render());

        try (AgentHolder holder = buildAgentIfEnabled(args)) {
            if (holder.agent instanceof LlmExternalPlayerAgent llmAgent) {
                String answer = llmAgent.explainMemory(session,
                        "Explain how your memory works for your own experiences and the places you have visited.");
                LOG.info("Explain-memory response:\n{}", answer);
            } else {
                LOG.info("LLM player agent not enabled; skipping explain-memory call.");
            }
        }
    }

    private static void runScriptedPath(PlayerToolEngine engine) {
        engine.invoke(PlayerToolRequest.look());
        engine.invoke(PlayerToolRequest.move(Direction8.N));
        engine.invoke(PlayerToolRequest.search());
        engine.invoke(PlayerToolRequest.move(Direction8.NE));
        engine.invoke(PlayerToolRequest.look());
    }

    private static AgentHolder buildAgentIfEnabled(String[] args) {
        boolean llmEnabled = isFlagEnabled("unna.player.llm.enabled", args)
                || isFlagEnabled("player.ai", args);
        if (!llmEnabled) {
            return new AgentHolder(null, null);
        }
        try {
            ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
                    .sources(PlayerAgentPromptConfig.class, PlayerAgentSpringConfig.class, AiConfig.class, OpenAiClientConfig.class)
                    .bannerMode(Banner.Mode.OFF)
                    .web(WebApplicationType.NONE)
                    .properties("spring.ai.openai.enabled=true", "SPRING_AI_OPENAI_ENABLED=true")
                    .run(args);
            org.springframework.ai.chat.client.ChatClient chatClient = ctx.getBean(org.springframework.ai.chat.client.ChatClient.class);
            String prompt = ctx.getBean("playerAgentSystemPrompt", String.class);
            java.util.Map<com.demo.island.game.PlayerTool, com.demo.island.player.ToolPrompt> toolPrompts = ctx.getBean("playerToolPrompts", java.util.Map.class);
            ExternalPlayerAgent agent = new LlmExternalPlayerAgent(chatClient, prompt, toolPrompts);
            return new AgentHolder(agent, ctx);
        } catch (Exception ex) {
            LOG.warn("MemoryExplainRunner: failed to initialize LLM agent; continuing without it. err={}", ex.getMessage());
            return new AgentHolder(null, null);
        }
    }

    private static boolean isFlagEnabled(String key, String[] args) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return Boolean.parseBoolean(sysProp);
        }
        String envKey = key.toUpperCase(java.util.Locale.ROOT).replace('.', '_').replace('-', '_');
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

    private record AgentHolder(ExternalPlayerAgent agent, ConfigurableApplicationContext context) implements AutoCloseable {
        @Override
        public void close() {
            if (context != null) {
                context.close();
            }
        }
    }
}
