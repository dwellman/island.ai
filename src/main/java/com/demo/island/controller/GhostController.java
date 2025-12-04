package com.demo.island.controller;

/**
 * Ghost controller placeholder. The intent simply signals that the ghost turn should run.
 */
public final class GhostController implements ActorController {

    private final String actorId;

    public GhostController(String actorId) {
        this.actorId = actorId;
    }

    @Override
    public ActorIntent decide(ActorView view) {
        return new ActorIntent(ActorIntent.Kind.GHOST_TURN, null);
    }

    public String getActorId() {
        return actorId;
    }
}
