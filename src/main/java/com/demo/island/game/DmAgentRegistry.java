package com.demo.island.game;

/**
 * Holds the current DM Agent (if any). Defaults to a no-op.
 */
final class DmAgentRegistry {
    private static final DmAgent NO_OP = context -> null;
    private static DmAgent agent = NO_OP;

    private DmAgentRegistry() {
    }

    static DmAgent getAgent() {
        return agent;
    }

    static void setAgent(DmAgent customAgent) {
        agent = customAgent == null ? NO_OP : customAgent;
    }

    static void reset() {
        agent = NO_OP;
    }
}
