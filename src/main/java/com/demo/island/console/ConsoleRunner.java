package com.demo.island.console;

import com.demo.island.core.WorldState;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.TurnEngine;
import com.demo.island.store.GameRepository;
import com.demo.island.store.InMemoryGameRepository;
import com.demo.island.world.WorldFactory;

import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConsoleRunner {

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.WARNING);

        GameRepository gameRepository = new InMemoryGameRepository();
        String sessionId = "session-" + UUID.randomUUID();
        WorldState worldState = WorldFactory.createDemoWorld(sessionId);
        gameRepository.createNewSession(worldState);

        DmAgent dmAgent = new SimpleDmStubAgent();
        TurnEngine turnEngine = new TurnEngine();

        Scanner scanner = new Scanner(System.in);
        String playerId = "player-1";

        System.out.println("Welcome to AI MUD Island (Demo). Type commands like: LOOK, GO N, TAKE MACHETE, DROP MACHETE, MOVE SKELETON, INVENTORY, QUIT.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null || line.trim().equalsIgnoreCase("QUIT")) {
                break;
            }

            PlayerCommand command = new PlayerCommand(playerId, line.trim());
            DmDecision decision = turnEngine.runTurn(worldState, command, dmAgent);
            System.out.println(decision.getNarration());
            System.out.println(statusLine(worldState));

            gameRepository.save(worldState);

            if (worldState.getSession().isMidnightReached()) {
                System.out.println("Midnight strikes. If the raft is not ready, hope fades.");
                break;
            }
        }

        System.out.println("Goodbye.");
    }

    private static String statusLine(WorldState worldState) {
        return "Turn " + worldState.getSession().getTurnNumber()
                + "/" + worldState.getSession().getMaxTurns()
                + " | Phase: " + worldState.getSession().getTimePhase()
                + (worldState.getSession().isGhostAwakened() ? " | The ghost stirs." : "");
    }
}
