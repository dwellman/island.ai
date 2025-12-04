package com.demo.island.player;

import com.demo.island.game.PlayerTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class PlayerAgentPromptConfig {

    @Bean
    public String playerAgentSystemPrompt(@Value("${unna.player.llm.lab:false}") boolean labMode) {
        if (labMode) {
            try {
                return Files.readString(Path.of("prompts/Player1.md"));
            } catch (IOException ex) {
                // fall through to default prompt if lab prompt cannot be loaded
            }
        }
        return """
                You are a smart external player controlling a single character in a text-based survival game set on a small island.
                The game engine is simple, “1980s-style” logic. It tracks locations, a countdown timer ([HH:MM]), items/resources, construction progress, and simple hazards.
                You are not in the story and not the DM. You are a 2025 player pressing buttons.
                Each turn you see the latest game message (with time) and a summarized state (time/phase, location, exits, visible items, inventory, progress, last tool/result).
                You must pick exactly one tool to use next and give a one-sentence reason. Do not narrate outcomes; just pick a tool and explain why.
                You have a limited number of turns per episode; avoid wasting time on actions that stop yielding new info or progress.
                """;
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
