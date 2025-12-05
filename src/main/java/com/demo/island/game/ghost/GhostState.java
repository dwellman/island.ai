package com.demo.island.game.ghost;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of the world passed to Ghost1.
 */
public record GhostState(
        int turnNumber,
        String time,
        String phase,
        PlayerView player,
        PlotView plot,
        GhostView ghost,
        PresenceView presence
) {

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("GHOST_STATE\n\n");
        sb.append("Turn: ").append(turnNumber).append("\n");
        sb.append("Time: ").append(time);
        if (phase != null && !phase.isBlank()) {
            sb.append(" (Phase: ").append(phase).append(")");
        }
        sb.append("\n\n");

        sb.append("Player:\n");
        sb.append("  Name: ").append(nvl(player.name())).append("\n");
        sb.append("  Location: ").append(nvl(player.locationName())).append(" ").append(nvl(player.coordinates())).append("\n");
        sb.append("  Inventory: ").append(list(player.inventoryNames())).append("\n\n");

        sb.append("Plot:\n");
        sb.append("  Id: ").append(nvl(plot.id())).append("\n");
        sb.append("  Name: ").append(nvl(plot.name())).append("\n");
        sb.append("  Biome: ").append(nvl(plot.biome())).append("\n");
        sb.append("  Region: ").append(nvl(plot.region())).append("\n");
        sb.append("  Description: ").append(nvl(plot.description())).append("\n");
        sb.append("  Exits: ").append(list(plot.exits())).append("\n");
        sb.append("  Visible items: ").append(list(plot.visibleItems())).append("\n\n");

        sb.append("Ghost:\n");
        sb.append("  Anchored here: ").append(ghost.anchoredHere()).append("\n");
        sb.append("  Anchored plots: ").append(list(ghost.anchoredPlots())).append("\n");
        sb.append("  Last mode: ").append(nvl(ghost.lastMode())).append("\n");
        sb.append("  Last text: ").append(nvl(ghost.lastText())).append("\n");
        sb.append("  Times manifested here: ").append(ghost.timesManifestedHere()).append("\n\n");

        sb.append("Presence trigger:\n");
        sb.append("  Present: ").append(presence.triggered()).append("\n");
        sb.append("  Reason: ").append(nvl(presence.reason())).append("\n");
        return sb.toString();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    private static String list(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        return "[" + String.join(", ", items) + "]";
    }

    public record PlayerView(String name, String locationName, String coordinates, List<String> inventoryNames) {}
    public record PlotView(String id, String name, String biome, String region, String description, List<String> exits, List<String> visibleItems) {}
    public record GhostView(boolean anchoredHere, List<String> anchoredPlots, String lastMode, String lastText, int timesManifestedHere) {}
    public record PresenceView(boolean triggered, String reason) {}
}
