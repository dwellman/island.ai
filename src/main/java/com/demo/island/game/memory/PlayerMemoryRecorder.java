package com.demo.island.game.memory;

import com.demo.island.dto.PlotContext;
import com.demo.island.game.GameSession;
import com.demo.island.world.IslandTile;

/**
 * Records memory snippets on plots and in the player's visit history.
 */
public final class PlayerMemoryRecorder {

    private PlayerMemoryRecorder() {
    }

    public static void recordVisit(GameSession session,
                                   PlotContext plotContext,
                                   String timeLabel) {
        if (session == null || plotContext == null) {
            return;
        }
        IslandTile tile = session.getMap().get(plotContext.plotId).orElse(null);
        if (tile == null) {
            return;
        }
        String memorySnippet = ensureMemorySnippet(tile, plotContext.currentDescription);
        String plotName = friendlyPlotName(tile);
        session.getPlayerMemory().recordVisit(tile.getTileId(), plotName, memorySnippet, plotContext.currentDescription, timeLabel);
    }

    private static String ensureMemorySnippet(IslandTile tile, String currentDescription) {
        String existing = tile.getPlayerMemoryNote();
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        String derived = deriveSnippet(currentDescription);
        tile.setPlayerMemoryNote(derived);
        return derived;
    }

    private static String deriveSnippet(String description) {
        if (description == null || description.isBlank()) {
            return "You have a faint memory but no details.";
        }
        String normalized = description.replaceAll("\\s+", " ").trim();
        int period = normalized.indexOf('.');
        if (period > 0) {
            normalized = normalized.substring(0, period + 1);
        }
        int limit = 180;
        if (normalized.length() > limit) {
            normalized = normalized.substring(0, limit) + "...";
        }
        return normalized;
    }

    private static String friendlyPlotName(IslandTile tile) {
        String id = tile.getTileId() == null ? "" : tile.getTileId();
        String biome = tile.getBiome() == null ? "" : tile.getBiome();
        if (!biome.isBlank()) {
            return id + " (" + biome.replace('_', ' ') + ")";
        }
        return id;
    }
}
