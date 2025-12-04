package com.demo.island.engine;

import com.demo.island.core.GameSession;
import com.demo.island.core.WorldState;

public final class SetFlagChange implements StateChange {

    private final FlagTarget target;
    private final String targetId;
    private final String flagName;
    private final boolean value;

    public SetFlagChange(FlagTarget target, String targetId, String flagName, boolean value) {
        this.target = target;
        this.targetId = targetId;
        this.flagName = flagName;
        this.value = value;
    }

    @Override
    public void applyTo(WorldState worldState) {
        switch (target) {
            case SESSION -> applyToSession(worldState.getSession());
            default -> {
                // Other targets can be added later
            }
        }
    }

    private void applyToSession(GameSession session) {
        if ("ghostAwakened".equals(flagName)) {
            session.setGhostAwakened(value);
        }
        if ("midnightReached".equals(flagName)) {
            session.setMidnightReached(value);
        }
    }
}
