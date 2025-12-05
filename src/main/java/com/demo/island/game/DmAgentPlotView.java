package com.demo.island.game;

import java.util.List;
import java.util.Map;

/**
 * Plot-focused view for DM Agent context.
 */
public record DmAgentPlotView(
        String plotId,
        String plotName,
        String biome,
        String region,
        String description,
        Map<String, String> exits,
        List<String> visibleItems
) {
}
