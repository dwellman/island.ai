package com.demo.island.game;

import com.demo.island.dto.PlotContext;

/**
 * Snapshot of a single turn to hand to an AI DM.
 */
public final class TurnContext {
    public String timePrefix;
    public CosmosClock.Phase phase;
    public int totalPips;
    public int maxPips;
    public GameStatus gameStatus;
    public GameEndReason gameEndReason;

    public String playerThingId;
    public String playerStatsSummary;
    public String playerInventorySummary;

    public String currentPlotId;
    public PlotContext plotContext;

    public GameActionType lastActionType;
    public String lastActionRawCommand;
    public String lastActionResultSummary;
    public boolean lastActionSuccess;

    public Challenge lastChallenge;
    public ChallengeResult lastChallengeResult;
}
