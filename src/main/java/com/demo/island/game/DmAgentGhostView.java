package com.demo.island.game;

/**
 * Ghost-related signal for DM Agent consumption.
 */
public record DmAgentGhostView(
        boolean present,
        String plotId,
        String eventText,
        String reason,
        String mode,
        String text
) {
}
