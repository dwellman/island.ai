package com.demo.island.game.memory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Tracks the player's recent visits with the remembered snippet for each plot.
 */
public final class PlayerMemory {

    private static final int DEFAULT_MAX_RECENT_VISITS = 5;
    private final int maxRecentVisits;
    private final LinkedList<VisitRecord> recentVisits = new LinkedList<>();
    private int visitCounter = 0;

    public PlayerMemory() {
        this(DEFAULT_MAX_RECENT_VISITS);
    }

    public PlayerMemory(int maxRecentVisits) {
        this.maxRecentVisits = Math.max(1, maxRecentVisits);
    }

    public void recordVisit(String plotId,
                            String plotName,
                            String memorySnippet,
                            String currentDescription,
                            String timeLabel) {
        if (plotId == null || plotId.isBlank()) {
            return;
        }
        String name = plotName == null || plotName.isBlank() ? plotId : plotName;
        String memory = normalize(memorySnippet);
        String current = normalize(currentDescription);
        String time = timeLabel == null ? "" : timeLabel;

        recentVisits.removeIf(v -> v.getPlotId().equals(plotId));
        recentVisits.addFirst(new VisitRecord(
                ++visitCounter,
                plotId,
                name,
                memory,
                current,
                time
        ));
        while (recentVisits.size() > maxRecentVisits) {
            recentVisits.removeLast();
        }
    }

    public List<VisitRecord> getRecentVisits() {
        return List.copyOf(recentVisits);
    }

    public int getMaxRecentVisits() {
        return maxRecentVisits;
    }

    private String normalize(String text) {
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

    /**
     * Snapshot of a single visit.
     */
    public static final class VisitRecord {
        private final int sequenceNumber;
        private final String plotId;
        private final String plotName;
        private final String memorySnippet;
        private final String lastSeenDescription;
        private final String visitTimeLabel;

        public VisitRecord(int sequenceNumber,
                           String plotId,
                           String plotName,
                           String memorySnippet,
                           String lastSeenDescription,
                           String visitTimeLabel) {
            this.sequenceNumber = sequenceNumber;
            this.plotId = Objects.requireNonNull(plotId);
            this.plotName = Objects.requireNonNull(plotName);
            this.memorySnippet = Objects.requireNonNull(memorySnippet);
            this.lastSeenDescription = Objects.requireNonNull(lastSeenDescription);
            this.visitTimeLabel = Objects.requireNonNull(visitTimeLabel);
        }

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        public String getPlotId() {
            return plotId;
        }

        public String getPlotName() {
            return plotName;
        }

        public String getMemorySnippet() {
            return memorySnippet;
        }

        public String getLastSeenDescription() {
            return lastSeenDescription;
        }

        public String getVisitTimeLabel() {
            return visitTimeLabel;
        }
    }
}
