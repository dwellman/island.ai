package com.demo.island.player;

import com.demo.island.config.PromptLoader;
import com.demo.island.game.PlayerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class PlayerAgentPromptConfig {

    private static final Logger LOG = LogManager.getLogger(PlayerAgentPromptConfig.class);
    private static final String DEFAULT_PATH = "prompts/Player1.md";
    private static final String FALLBACK_PROMPT = """
            You are an external player for a small text-based survival game. Choose exactly one tool each turn (LOOK, MOVE, SEARCH, TAKE, DROP, RAFT_WORK, STATUS) and provide short reasoning. Keep responses concise and avoid free-form narration.
            """;

    @Bean
    public String playerAgentSystemPrompt() {
        String overridePath = System.getProperty("player1.prompt.path");
        if (overridePath == null || overridePath.isBlank()) {
            overridePath = System.getenv("PLAYER1_PROMPT_PATH");
        }
        return PromptLoader.loadOrFallback(DEFAULT_PATH, overridePath, FALLBACK_PROMPT, LOG, "Player1");
    }

    @Bean
    public Map<PlayerTool, ToolPrompt> playerToolPrompts() {
        Map<PlayerTool, ToolPrompt> prompts = new EnumMap<>(PlayerTool.class);
        prompts.put(PlayerTool.LOOK, new ToolPrompt(
                "Get a more detailed description of your current location and what’s immediately visible (environment, items, exits).",
                "Args: none.",
                "Small but nonzero time cost; use to orient before choosing direction or another action."
        ));
        prompts.put(PlayerTool.MOVE, new ToolPrompt(
                "Walk from your current location to an adjacent one in a specified direction.",
                "Args: one direction (N, NE, E, SE, S, SW, W, NW).",
                "Small time cost; choose directions that seem promising based on prior descriptions and known exits."
        ));
        prompts.put(PlayerTool.SEARCH, new ToolPrompt(
                "Carefully inspect the current location for useful items or hidden details.",
                "Args: none.",
                "Small-to-moderate time cost; use when you suspect resources or clues nearby, avoid repeating if nothing new appears."
        ));
        prompts.put(PlayerTool.TAKE, new ToolPrompt(
                "Pick up a visible item at the current location.",
                "Args: one item name (e.g., HATCHET, WOOD_LOG, VINE_ROPE, METAL_SCRAP).",
                "Small time cost; use when you see something useful and have a clear reason to carry it."
        ));
        prompts.put(PlayerTool.DROP, new ToolPrompt(
                "Put down an item from your inventory at the current location.",
                "Args: one item name (e.g., HATCHET, WOOD_LOG, VINE_ROPE, METAL_SCRAP).",
                "Small time cost; use to manage inventory or stage resources at a spot that seems strategically useful."
        ));
        prompts.put(PlayerTool.RAFT_WORK, new ToolPrompt(
                "Attempt construction work at your current location using materials you’re carrying.",
                "Args: none.",
                "Moderate time cost; use when you believe you have enough materials and are at a place where working on something large would make sense (camp or shoreline)."
        ));
        prompts.put(PlayerTool.STATUS, new ToolPrompt(
                "Get a concise summary of your current state (time, location, inventory, progress) without changing the world.",
                "Args: none.",
                "Minimal or no time cost; use to re-center and plan before choosing your next action."
        ));
        return prompts;
    }
}
