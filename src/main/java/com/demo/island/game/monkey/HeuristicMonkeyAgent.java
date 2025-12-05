package com.demo.island.game.monkey;

import com.demo.island.game.AgentMood;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.PlayerTool;
import com.demo.island.world.Direction8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simple deterministic-ish monkey agent: look on turn 1, then wander exits in order.
 */
public final class HeuristicMonkeyAgent implements MonkeyAgent {

    private final Random random = new Random();

    @Override
    public MonkeyDecision decide(MonkeyState state) {
        if (state == null || state.exits() == null) {
            return new MonkeyDecision(PlayerToolRequest.search(), "", "Check surroundings.", AgentMood.CURIOUS);
        }
        if (state.turnNumber() <= 1) {
            return new MonkeyDecision(PlayerToolRequest.look(), "", "Scout the area.", AgentMood.CURIOUS);
        }
        List<String> dirs = new ArrayList<>(state.exits().keySet());
        if (dirs.isEmpty()) {
            return new MonkeyDecision(PlayerToolRequest.look(), "", "No exits; staying put.", AgentMood.CONFUSED);
        }
        String dir = dirs.get(random.nextInt(dirs.size()));
        Direction8 direction = Direction8.valueOf(dir);
        PlayerToolRequest req = PlayerToolRequest.move(direction);
        return new MonkeyDecision(req, dir, "Explore " + dir + ".", AgentMood.CURIOUS);
    }
}
