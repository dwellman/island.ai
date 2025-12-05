package com.demo.island.game.ghost;

/**
 * Result returned by a GhostAgent.
 */
public record GhostManifestation(GhostMode mode, String text) {
    public static GhostManifestation silent() {
        return new GhostManifestation(GhostMode.SILENT, "");
    }
}
