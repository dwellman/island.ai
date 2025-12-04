package com.demo.island.game;

/**
 * External AI/test driver that chooses player tools based on session state and the last tool result.
 */
public interface ExternalPlayerAgent {

    AgentDecision decideNext(GameSession session, PlayerToolResult lastResult);
}
