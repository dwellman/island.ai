package com.demo.island.tools;

import com.demo.island.core.WorldState;
import com.demo.island.engine.check.CheckRequest;
import com.demo.island.engine.check.CheckSubjectKind;
import com.demo.island.engine.check.CheckType;
import com.demo.island.ghost.GhostIntent;

/**
 * Helper for ghost-specific intent/check construction.
 */
public final class GhostTools {

    public CheckRequest hearingCheck(String ghostId, int dc) {
        return new CheckRequest(CheckType.HEARING, CheckSubjectKind.CREATURE, ghostId, dc);
    }

    public String firstPlayerTile(WorldState worldState) {
        return worldState.getPlayers().values().stream()
                .findFirst()
                .map(com.demo.island.core.Player::getCurrentTileId)
                .orElse(null);
    }

    public GhostIntent checkIntent(CheckRequest request) {
        return GhostIntent.check(request.getType().name(), request.getSubjectKind().name(),
                request.getSubjectId(), request.getDifficulty());
    }

    public GhostIntent setTargetIntent(String creatureId, String targetTileId) {
        return GhostIntent.setTargetTile(creatureId, targetTileId);
    }
}
