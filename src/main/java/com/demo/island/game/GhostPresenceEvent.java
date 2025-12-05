package com.demo.island.game;

import com.demo.island.game.ghost.GhostMode;

/**
 * Records a single ghost presence signal for a turn.
 */
public record GhostPresenceEvent(String plotId,
                                 String eventText,
                                 String reason,
                                 GhostMode mode,
                                 String manifestationText) {
}
