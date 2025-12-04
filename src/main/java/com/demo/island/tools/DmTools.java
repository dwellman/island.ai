package com.demo.island.tools;

import com.demo.island.dto.StateChangeDto;

/**
 * Helper for building action DTOs and check requests for the DM agent pipeline.
 * Keeps verb construction consistent and centralized.
 */
public final class DmTools {

    public StateChangeDto movePlayer(String playerId, String toTileId) {
        return StateChangeDto.movePlayer(playerId, toTileId);
    }

    public StateChangeDto transferItem(String itemId, String ownerKind, String ownerId, String containedByItemId) {
        return StateChangeDto.transferItem(itemId, ownerKind, ownerId, containedByItemId);
    }

    public StateChangeDto setFlagSession(String flagName, boolean value) {
        return StateChangeDto.setFlag("SESSION", flagName, value);
    }

    public StateChangeDto runItemHook(String hookId, String itemId) {
        return StateChangeDto.runItemHook(hookId, itemId);
    }

    public StateChangeDto check(String checkType, String subjectKind, String subjectId, int difficulty) {
        return StateChangeDto.check(checkType, subjectKind, subjectId, difficulty);
    }
}
