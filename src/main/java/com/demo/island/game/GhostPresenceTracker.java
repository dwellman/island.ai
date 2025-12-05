package com.demo.island.game;

import com.demo.island.game.ghost.GhostMode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Deterministic ghost presence detector for anchored plots.
 */
public final class GhostPresenceTracker {

    private static final String DEFAULT_REASON = "PLAYER_ENTERED_ANCHOR";
    private static final String DEFAULT_TEXT = "An icy chill brushes past; something spectral stirs nearby.";
    private static final Map<String, String> ANCHOR_TEXT = Map.of(
            "T_OLD_RUINS", "A pale shape flickers in the corner of your eye near the crumbling stones.",
            "T_CAVE_ENTRANCE", "Cold air carries a whisper from the cave; a faint figure hovers in the dark."
    );

    private GhostPresenceTracker() {
    }

    public static Optional<GhostPresenceEvent> maybeTrigger(GameSession session, boolean actionSuccess) {
        if (session == null || session.getLocation() == null || !actionSuccess) {
            return Optional.empty();
        }
        String plotId = session.getLocation().getTileId();
        if (!isGhostAnchor(plotId) || session.isGhostPresenceTriggered(plotId)) {
            return Optional.empty();
        }
        session.markGhostPresenceTriggered(plotId);
        String text = ANCHOR_TEXT.getOrDefault(plotId, DEFAULT_TEXT);
        return Optional.of(new GhostPresenceEvent(plotId, text, DEFAULT_REASON, GhostMode.PRESENCE_ONLY, ""));
    }

    public static boolean isGhostAnchor(String plotId) {
        return plotId != null && ANCHOR_TEXT.containsKey(plotId);
    }

    public static Set<String> anchorPlotIds() {
        return ANCHOR_TEXT.keySet();
    }
}
