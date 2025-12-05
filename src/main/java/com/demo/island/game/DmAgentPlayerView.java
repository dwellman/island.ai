package com.demo.island.game;

import java.util.List;

/**
 * Player-focused view for DM Agent context.
 */
public record DmAgentPlayerView(
        String label,
        List<String> inventory,
        Integer hp,
        Integer maxHp,
        List<String> recentMoods
) {
}
