package com.demo.island.game.memory;

import com.demo.island.game.ContextBuilder;
import com.demo.island.game.GameSession;
import com.demo.island.game.PlayerToolEngine;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.world.PlayerLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemorySummaryBuilderTest {

    @Test
    void recordsRecentVisitsInOrder() {
        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);

        engine.invoke(PlayerToolRequest.look());

        session.setLocation(new PlayerLocation("T_CAMP"));
        PlayerMemoryRecorder.recordVisit(session, ContextBuilder.buildPlotContext(session), "[TEST]");

        MemorySummary summary = MemorySummaryBuilder.build(session);
        assertFalse(summary.getEntries().isEmpty());
        assertEquals("T_CAMP", summary.getEntries().get(0).plotId());
    }

    @Test
    void marksMismatchWhenWorldChanges() {
        GameSession session = GameSession.newSession();
        PlayerToolEngine engine = new PlayerToolEngine(session);

        engine.invoke(PlayerToolRequest.look());

        var beachTile = session.getMap().get("T_WRECK_BEACH").orElseThrow();
        beachTile.getContext().setCurrentDescription("Charred remains and blackened sand.");

        MemorySummary summary = MemorySummaryBuilder.build(session);
        MemorySummary.MemoryEntry beachMemory = summary.getEntries().stream()
                .filter(e -> e.plotId().equals("T_WRECK_BEACH"))
                .findFirst()
                .orElseThrow();
        assertTrue(beachMemory.differsFromCurrent());
    }
}
