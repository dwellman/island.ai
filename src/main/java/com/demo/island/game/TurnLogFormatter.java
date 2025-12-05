package com.demo.island.game;

/**
 * Formats high-level per-turn log lines with time and coordinates.
 */
public final class TurnLogFormatter {

    private TurnLogFormatter() {
    }

    public static String format(GameSession session, String plotIdOverride, String message) {
        String time = session.getClock().formatRemainingBracketed();
        String coords = "(?, ?)";
        String tileId = plotIdOverride != null
                ? plotIdOverride
                : (session.getLocation() != null ? session.getLocation().getTileId() : null);
        if (tileId != null) {
            var tileOpt = session.getMap().get(tileId);
            if (tileOpt.isPresent()) {
                var pos = tileOpt.get().getPosition();
                coords = "(" + pos.x() + ", " + pos.y() + ")";
            }
        }
        return time + " " + coords + " -- " + message;
    }
}
