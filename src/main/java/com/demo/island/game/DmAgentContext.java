package com.demo.island.game;

/**
 * Structured view handed to the DM Agent each turn.
 */
public record DmAgentContext(
        Integer turnNumber,
        String time,
        String phase,
        DmAgentPlayerView player,
        DmAgentPlotView plot,
        DmAgentActionOutcome actionOutcome,
        DmAgentGhostView ghost
) { }
