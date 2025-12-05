package com.demo.island.game;

/**
 * Small helper to decide whether the DM Agent seam is enabled.
 */
public final class DmAgentConfig {

    private static Boolean enabledOverride;

    private DmAgentConfig() {
    }

    public static boolean isEnabled() {
        if (enabledOverride != null) {
            return enabledOverride;
        }
        String prop = System.getProperty("dm.agent.enabled");
        if (prop == null || prop.isBlank()) {
            prop = System.getenv("DM_AGENT_ENABLED");
        }
        return prop != null && Boolean.parseBoolean(prop);
    }

    public static void setEnabledOverride(Boolean enabled) {
        enabledOverride = enabled;
    }
}
