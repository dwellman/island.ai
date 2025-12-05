package com.demo.island.game;

/**
 * Optional DM agent that can rewrite DM body text for a turn.
 */
public interface DmAgent {
    /**
     * @param context structured snapshot of the player, plot, and action outcome
     * @return replacement DM body text, or null/blank to fall back to the core DM text
     */
    String rewrite(DmAgentContext context);
}
