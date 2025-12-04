package com.demo.island.engine.check;

import com.demo.island.core.Creature;
import com.demo.island.core.GameSession;
import com.demo.island.core.Player;
import com.demo.island.core.WorldState;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class CheckConfig {

    @FunctionalInterface
    public interface Rule extends BiFunction<WorldState, CheckRequest, Integer> {
    }

    private final Map<CheckType, Rule> rules;

    public CheckConfig(Map<CheckType, Rule> rules) {
        this.rules = rules;
    }

    public int modifierFor(WorldState worldState, CheckRequest request) {
        Rule rule = rules.getOrDefault(request.getType(), (ws, req) -> 0);
        return rule.apply(worldState, request);
    }

    public static CheckConfig defaultConfig() {
        Map<CheckType, Rule> map = new EnumMap<>(CheckType.class);
        map.put(CheckType.PERCEPTION, (ws, req) -> stat(ws, req, "AWR"));
        map.put(CheckType.INTIMIDATION, (ws, req) -> stat(ws, req, "CHA"));
        map.put(CheckType.CLIMB, (ws, req) -> stat(ws, req, "AGI"));
        map.put(CheckType.QUICKSAND, (ws, req) -> stat(ws, req, "AGI"));
        map.put(CheckType.HEARING, CheckConfig::hearingModifier);
        map.put(CheckType.GENERIC, (ws, req) -> 0);
        return new CheckConfig(map);
    }

    private static int stat(WorldState worldState, CheckRequest request, String statName) {
        return switch (request.getSubjectKind()) {
            case PLAYER -> {
                Player player = worldState.getPlayer(request.getSubjectId());
                yield player != null ? player.getStats().getOrDefault(statName, 0) : 0;
            }
            case CREATURE -> {
                Creature creature = worldState.getCreature(request.getSubjectId());
                yield creature != null ? creature.getStats().getOrDefault(statName, 0) : 0;
            }
        };
    }

    private static int hearingModifier(WorldState worldState, CheckRequest request) {
        int base = stat(worldState, request, "AWR");
        GameSession.TimePhase phase = worldState.getSession().getTimePhase();
        int nightBonus = phase == GameSession.TimePhase.DARK ? 2 : 0;
        return base + nightBonus;
    }
}
