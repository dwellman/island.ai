package com.demo.island.sim;

import com.demo.island.game.AgentDecision;
import com.demo.island.game.AgentMood;
import com.demo.island.game.PlayerTool;
import com.demo.island.game.PlayerToolRequest;

public final class AgentDecisionLog {
    public int turnIndex;
    public String time;
    public PlayerTool tool;
    public PlayerToolRequest request;
    public String reason;
    public AgentMood mood;
    public String note;

    public static AgentDecisionLog from(int turnIndex, String time, AgentDecision decision) {
        AgentDecisionLog log = new AgentDecisionLog();
        log.turnIndex = turnIndex;
        log.time = time;
        log.tool = decision.getRequest().getTool();
        log.request = decision.getRequest();
        log.reason = decision.getReason();
        log.mood = decision.getMood();
        log.note = decision.getNote();
        return log;
    }
}
