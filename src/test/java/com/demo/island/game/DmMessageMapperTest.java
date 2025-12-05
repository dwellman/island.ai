package com.demo.island.game;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DmMessageMapperTest {

    @Test
    void mapsKnownReasons() {
        assertThat(DmMessageMapper.messageFor(
                new ToolOutcome(OutcomeType.BLOCKED, ReasonCode.NO_VISIBLE_ITEMS_HERE, "", null, null, null),
                "[00:00]"))
                .contains("haven't found any items here");
        assertThat(DmMessageMapper.messageFor(
                new ToolOutcome(OutcomeType.BLOCKED, ReasonCode.NO_EXIT_IN_DIRECTION, "", null, null, null),
                "[00:00]"))
                .contains("can't go that way");
        assertThat(DmMessageMapper.messageFor(
                new ToolOutcome(OutcomeType.BLOCKED, ReasonCode.ALREADY_CARRYING_ITEM, "", null, null, null),
                "[00:00]"))
                .contains("already carrying");
    }

    @Test
    void unknownReasonFallsBack() {
        assertThat(DmMessageMapper.messageFor(
                new ToolOutcome(OutcomeType.BLOCKED, ReasonCode.UNKNOWN, "", null, null, null),
                "[00:00]"))
                .contains("You can't do that.");
    }
}
