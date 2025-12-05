package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.world.CharacterThing;
import com.demo.island.world.Thing;
import com.demo.island.world.WorldThingIndex;

import java.util.StringJoiner;

public final class TurnContextBuilder {

    private TurnContextBuilder() {
    }

    public static TurnContext build(GameSession session,
                                    GameAction lastAction,
                                    String lastResultSummary,
                                    boolean lastSuccess,
                                    Challenge lastChallenge,
                                    ChallengeResult lastChallengeResult) {
        return build(session, lastAction, lastResultSummary, lastSuccess, lastChallenge, lastChallengeResult, null, session.getLocation().getTileId(), ContextBuilder.buildPlotContext(session));
    }

    public static TurnContext build(GameSession session,
                                    GameAction lastAction,
                                    String lastResultSummary,
                                    boolean lastSuccess,
                                    Challenge lastChallenge,
                                    ChallengeResult lastChallengeResult,
                                    GhostPresenceEvent ghostEvent) {
        return build(session, lastAction, lastResultSummary, lastSuccess, lastChallenge, lastChallengeResult, ghostEvent, session.getLocation().getTileId(), ContextBuilder.buildPlotContext(session));
    }

    public static TurnContext build(GameSession session,
                                    GameAction lastAction,
                                    String lastResultSummary,
                                    boolean lastSuccess,
                                    Challenge lastChallenge,
                                    ChallengeResult lastChallengeResult,
                                    GhostPresenceEvent ghostEvent,
                                    String plotIdOverride,
                                    com.demo.island.dto.PlotContext plotContextOverride) {
        TurnContext ctx = new TurnContext();
        CosmosClock clock = session.getClock();
        ctx.timePrefix = clock.formatRemainingBracketed();
        ctx.phase = clock.getPhase();
        ctx.totalPips = clock.getTotalPips();
        ctx.maxPips = clock.getMaxPips();
        ctx.gameStatus = session.getStatus();
        ctx.gameEndReason = session.getGameEndReason();

        WorldThingIndex index = session.getThingIndex();
        Thing player = index.getThing("THING_PLAYER");
        ctx.playerThingId = player != null ? player.getId() : "THING_PLAYER";
        ctx.playerStatsSummary = playerStatsSummary(player);
        ctx.playerInventorySummary = inventorySummary(session);

        ctx.currentPlotId = plotIdOverride;
        ctx.plotContext = plotContextOverride;

        ctx.lastActionType = lastAction.getType();
        ctx.lastActionRawCommand = null; // raw text not tracked yet
        ctx.lastActionResultSummary = lastResultSummary;
        ctx.lastActionSuccess = lastSuccess;
        ctx.lastChallenge = lastChallenge;
        ctx.lastChallengeResult = lastChallengeResult;

        applyGhostEvent(ctx, ghostEvent);
        return ctx;
    }

    private static void applyGhostEvent(TurnContext ctx, GhostPresenceEvent ghostEvent) {
        if (ghostEvent == null) {
            return;
        }
        ctx.ghostEventTriggered = true;
        ctx.ghostEventPlotId = ghostEvent.plotId();
        ctx.ghostEventText = ghostEvent.eventText();
        ctx.ghostEventReason = ghostEvent.reason();
        ctx.ghostMode = ghostEvent.mode() == null ? null : ghostEvent.mode().name();
        ctx.ghostText = ghostEvent.manifestationText();
    }

    private static String playerStatsSummary(Thing player) {
        if (!(player instanceof CharacterThing ct)) {
            return "";
        }
        return "HP " + ct.getHp() + "/" + ct.getMaxHp();
    }

    private static String inventorySummary(GameSession session) {
        StringJoiner joiner = new StringJoiner(", ");
        session.getInventory().forEach((item, count) -> {
            if (count > 0) {
                joiner.add(item + " x" + count);
            }
        });
        return joiner.toString();
    }
}
