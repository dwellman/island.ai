package com.demo.island.game;

/**
 * Outcome snapshot for DM Agent consumption.
 */
public record DmAgentActionOutcome(
        String toolName,
        String toolTarget,
        OutcomeType outcomeType,
        ReasonCode reasonCode,
        String coreDmText,
        String challengeSummary
) {
}
