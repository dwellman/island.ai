package com.demo.island.game;

public interface DmAdapter {

    /**
     * Produce the body of the narration (without the time prefix) for the latest turn.
     */
    String narrate(TurnContext context);
}
