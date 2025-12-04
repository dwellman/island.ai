package com.demo.island.controller;

import com.demo.island.engine.TurnEngine;
import com.demo.island.player.PlayerAgent;
import com.demo.island.player.PlayerInput;

/**
 * AI controller that delegates to the PlayerAgent (LLM or stub) to produce a command string.
 */
public final class PlayerAiController implements ActorController {

    private final PlayerAgent playerAgent;
    private final TurnEngine turnEngine;
    private final String playerId;

    public PlayerAiController(PlayerAgent playerAgent, TurnEngine turnEngine, String playerId) {
        this.playerAgent = playerAgent;
        this.turnEngine = turnEngine;
        this.playerId = playerId;
    }

    @Override
    public ActorIntent decide(ActorView view) {
        PlayerInput input = new PlayerInput(view.worldState(), playerId, turnEngine.getRecentCheckResults(), null);
        String commandText = playerAgent.decide(input);
        return new ActorIntent(ActorIntent.Kind.PLAYER_COMMAND, commandText);
    }
}
