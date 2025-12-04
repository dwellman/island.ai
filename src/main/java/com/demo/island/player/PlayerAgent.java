package com.demo.island.player;

public interface PlayerAgent {

    /**
     * Decide a single command string for the given player snapshot.
     */
    String decide(PlayerInput input);
}
