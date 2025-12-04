package com.demo.island.store;

import com.demo.island.core.WorldState;

import java.util.List;
import java.util.Optional;

public interface GameRepository {

    WorldState createNewSession(WorldState initialWorldState);

    Optional<WorldState> findBySessionId(String sessionId);

    List<WorldState> findAllSessions();

    void save(WorldState worldState);
}
