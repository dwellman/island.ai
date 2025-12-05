package com.demo.island.game;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Formats DmAgentContext into the DM_AGENT_CONTEXT text block expected by DM1.
 */
final class DmAgentContextFormatter {

    private DmAgentContextFormatter() {
    }

    static String format(DmAgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("DM_AGENT_CONTEXT\n\n");
        sb.append("Turn: ").append(nvl(context.turnNumber(), "Unknown")).append("\n");
        sb.append("Time: ").append(nvl(context.time(), "Unknown"));
        if (context.phase() != null && !context.phase().isBlank()) {
            sb.append(" (Phase: ").append(context.phase()).append(")");
        }
        sb.append("\n\n");

        DmAgentPlayerView player = context.player();
        sb.append("Player:\n");
        sb.append("  Name: ").append(nvl(player.label(), "Player 1")).append("\n");
        if (player.hp() != null && player.maxHp() != null) {
            sb.append("  HP: ").append(player.hp()).append("/").append(player.maxHp()).append("\n");
        }
        sb.append("  Inventory: [").append(joinList(player.inventory())).append("]\n");
        if (player.recentMoods() != null && !player.recentMoods().isEmpty()) {
            sb.append("  Recent moods: [").append(joinList(player.recentMoods())).append("]\n");
        }
        sb.append("\n");

        DmAgentPlotView plot = context.plot();
        sb.append("Plot:\n");
        sb.append("  Id: ").append(nvl(plot.plotId(), "Unknown")).append("\n");
        sb.append("  Name: ").append(nvl(plot.plotName(), "Unknown")).append("\n");
        sb.append("  Biome: ").append(nvl(plot.biome(), "Unknown")).append("\n");
        sb.append("  Region: ").append(nvl(plot.region(), "Unknown")).append("\n");
        sb.append("  Description: ").append(nvl(plot.description(), "Unknown")).append("\n");
        sb.append("  Exits: [").append(joinMap(plot.exits())).append("]\n");
        sb.append("  Visible items: [").append(joinList(plot.visibleItems())).append("]\n\n");

        DmAgentActionOutcome action = context.actionOutcome();
        sb.append("Action:\n");
        sb.append("  Tool: ").append(nvl(action.toolName(), "Unknown")).append("\n");
        sb.append("  Target: ").append(nvl(action.toolTarget(), "")).append("\n");
        sb.append("  OutcomeType: ").append(nvl(action.outcomeType(), "Unknown")).append("\n");
        sb.append("  ReasonCode: ").append(nvl(action.reasonCode(), "Unknown")).append("\n");
        sb.append("  CoreDM: ").append(nvl(stripNewlines(action.coreDmText()), "Unknown")).append("\n");
        sb.append("  Challenge: ").append(nvl(stripNewlines(action.challengeSummary()), "")).append("\n\n");

        DmAgentGhostView ghost = context.ghost();
        sb.append("Ghost:\n");
        boolean present = ghost != null && ghost.present();
        sb.append("  Present: ").append(present).append("\n");
        if (present) {
            sb.append("  Plot: ").append(nvl(ghost.plotId(), "Unknown")).append("\n");
            sb.append("  Event: ").append(nvl(stripNewlines(ghost.eventText()), "")).append("\n");
            sb.append("  Reason: ").append(nvl(ghost.reason(), "")).append("\n");
            sb.append("  Mode: ").append(nvl(ghost.mode(), "")).append("\n");
            if (ghost.text() != null && !ghost.text().isBlank()) {
                sb.append("  Manifestation: ").append(nvl(stripNewlines(ghost.text()), "")).append("\n");
            }
        }
        sb.append("\n");

        sb.append("Notes:\n");
        sb.append("  - All of the information above is authoritative. You must not contradict it.\n");
        sb.append("  - OutcomeType and ReasonCode are the final verdict from the game engine.\n");
        return sb.toString();
    }

    private static String joinList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return String.join(", ", items);
    }

    private static String joinMap(Map<String, String> exits) {
        if (exits == null || exits.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        exits.forEach((dir, name) -> {
            if (name == null || name.isBlank()) {
                joiner.add(dir);
            } else {
                joiner.add(dir + " (" + name + ")");
            }
        });
        return joiner.toString();
    }

    private static String nvl(Object value, String fallback) {
        if (value == null) return fallback;
        String s = value.toString();
        return s.isBlank() ? fallback : s;
    }

    private static String stripNewlines(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s+", " ").trim();
    }
}
