package com.demo.island.store;

import com.demo.island.core.WorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryGameRepository implements GameRepository {

    private final ConcurrentMap<String, WorldState> sessions = new ConcurrentHashMap<>();

    @Override
    public WorldState createNewSession(WorldState initialWorldState) {
        String sessionId = initialWorldState.getSession().getSessionId();
        sessions.put(sessionId, initialWorldState);
        return initialWorldState;
    }

    @Override
    public Optional<WorldState> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<WorldState> findAllSessions() {
        return new ArrayList<>(sessions.values());
    }

    @Override
    public void save(WorldState worldState) {
        String sessionId = worldState.getSession().getSessionId();
        sessions.put(sessionId, worldState);
    }
}
