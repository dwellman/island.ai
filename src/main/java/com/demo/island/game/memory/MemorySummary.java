package com.demo.island.game.memory;

import java.util.List;
import java.util.Objects;

/**
 * Structured summary of player memory suitable for prompt inclusion.
 */
public final class MemorySummary {

    private final String metaDescription;
    private final List<MemoryEntry> entries;
    private final String currentPlotMismatchNote;

    public MemorySummary(String metaDescription,
                         List<MemoryEntry> entries,
                         String currentPlotMismatchNote) {
        this.metaDescription = metaDescription == null ? "" : metaDescription;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
        this.currentPlotMismatchNote = currentPlotMismatchNote == null ? "" : currentPlotMismatchNote;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public List<MemoryEntry> getEntries() {
        return entries;
    }

    public String getCurrentPlotMismatchNote() {
        return currentPlotMismatchNote;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    /**
     * Render a short deterministic summary for prompts.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("How memory works: ").append(metaDescription).append("\n");
        sb.append("Recent visits (most recent first):\n");
        if (entries.isEmpty()) {
            sb.append("  None recorded yet.\n");
        } else {
            for (MemoryEntry entry : entries) {
                sb.append("  - ").append(entry.plotName())
                        .append(" (id ").append(entry.plotId()).append(", last at ").append(entry.lastVisited()).append("): ")
                        .append(entry.memorySnippet()).append("\n");
                if (entry.differsFromCurrent()) {
                    sb.append("    Now: ").append(entry.currentDescription()).append(" [memory may be out of date]\n");
                }
            }
        }
        if (!currentPlotMismatchNote.isBlank()) {
            sb.append("Current plot check: ").append(currentPlotMismatchNote).append("\n");
        }
        return sb.toString().trim();
    }

    public record MemoryEntry(String plotId,
                              String plotName,
                              String memorySnippet,
                              String currentDescription,
                              String lastVisited,
                              boolean differsFromCurrent) {

        public MemoryEntry {
            plotId = Objects.requireNonNullElse(plotId, "");
            plotName = Objects.requireNonNullElse(plotName, plotId);
            memorySnippet = Objects.requireNonNullElse(memorySnippet, "");
            currentDescription = Objects.requireNonNullElse(currentDescription, "");
            lastVisited = Objects.requireNonNullElse(lastVisited, "");
        }
    }
}
