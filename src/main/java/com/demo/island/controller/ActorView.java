package com.demo.island.controller;

import com.demo.island.core.WorldState;

public record ActorView(WorldState worldState, String actorId) {
}
