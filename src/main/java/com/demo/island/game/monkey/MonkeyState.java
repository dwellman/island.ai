package com.demo.island.game.monkey;

import java.util.Map;

public record MonkeyState(
        int turnNumber,
        String actorId,
        String plotId,
        String plotName,
        String description,
        Map<String, String> exits,
        boolean playerHere
) {
}
