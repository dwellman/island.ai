package com.demo.island.controller;

/**
 * Monkey controller placeholder. The intent signals a monkey turn should run.
 */
public final class MonkeyController implements ActorController {

    private final String actorId;

    public MonkeyController(String actorId) {
        this.actorId = actorId;
    }

    @Override
    public ActorIntent decide(ActorView view) {
        return new ActorIntent(ActorIntent.Kind.MONKEY_TURN, null);
    }

    public String getActorId() {
        return actorId;
    }
}
