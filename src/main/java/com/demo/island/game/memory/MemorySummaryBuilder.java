package com.demo.island.game.memory;

import com.demo.island.game.GameSession;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Builds a deterministic, prompt-safe summary of memory state.
 */
public final class MemorySummaryBuilder {

    private MemorySummaryBuilder() {
    }

    public static MemorySummary build(GameSession session) {
        if (session == null) {
            return new MemorySummary(
                    "Memory unavailable.",
                    List.of(),
                    ""
            );
        }
        return build(session, session.getPlayerMemory());
    }

    public static MemorySummary build(GameSession session, PlayerMemory playerMemory) {
        if (session == null || playerMemory == null) {
            return new MemorySummary(
                    "Memory unavailable.",
                    List.of(),
                    ""
            );
        }

        IslandMap map = session.getMap();
        List<MemorySummary.MemoryEntry> entries = new ArrayList<>();
        for (PlayerMemory.VisitRecord visit : playerMemory.getRecentVisits()) {
            String currentDescription = currentDescription(map, visit.getPlotId(), visit.getLastSeenDescription());
            boolean differs = differsFromMemory(visit.getMemorySnippet(), currentDescription);
            entries.add(new MemorySummary.MemoryEntry(
                    visit.getPlotId(),
                    visit.getPlotName(),
                    visit.getMemorySnippet(),
                    currentDescription,
                    visit.getVisitTimeLabel(),
                    differs
            ));
        }
        String currentPlotId = session.getLocation() != null ? session.getLocation().getTileId() : "";
        String mismatchNote = mismatchNoteForCurrent(entries, currentPlotId);
        String meta = "You remember places as short notes on each plot you have visited. "
                + "The list keeps the last " + playerMemory.getMaxRecentVisits()
                + " visits and can be out of date if the island changed. "
                + "When you return, compare the memory snippet to what you see now.";
        return new MemorySummary(meta, entries, mismatchNote);
    }

    private static String currentDescription(IslandMap map, String plotId, String fallback) {
        if (map == null || plotId == null) {
            return fallback == null ? "" : fallback;
        }
        Optional<IslandTile> tileOpt = map.get(plotId);
        if (tileOpt.isEmpty()) {
            return fallback == null ? "" : fallback;
        }
        IslandTile tile = tileOpt.get();
        if (tile.getContext() != null && tile.getContext().getCurrentDescription() != null) {
            return normalize(tile.getContext().getCurrentDescription());
        }
        return fallback == null ? "" : fallback;
    }

    private static boolean differsFromMemory(String memorySnippet, String currentDescription) {
        String memory = normalize(memorySnippet);
        String current = normalize(currentDescription);
        if (memory.isBlank() || current.isBlank()) {
            return false;
        }
        return !memory.equalsIgnoreCase(current);
    }

    private static String mismatchNoteForCurrent(List<MemorySummary.MemoryEntry> entries, String currentPlotId) {
        if (currentPlotId == null || currentPlotId.isBlank()) {
            return "";
        }
        return entries.stream()
                .filter(e -> currentPlotId.equals(e.plotId()))
                .findFirst()
                .filter(MemorySummary.MemoryEntry::differsFromCurrent)
                .map(e -> "Your memory says: \"" + e.memorySnippet()
                        + "\" but right now it looks like: \"" + e.currentDescription() + "\".")
                .orElse("");
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        int limit = 240;
        if (clean.length() > limit) {
            return clean.substring(0, limit) + "...";
        }
        return clean;
    }
}
