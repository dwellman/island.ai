package com.demo.island.console;

import com.demo.island.core.WorldState;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.TurnEngine;
import com.demo.island.player.PlayerAgent;
import com.demo.island.store.GameRepository;
import com.demo.island.world.WorldFactory;
import com.demo.island.controller.ActorController;
import com.demo.island.controller.ActorIntent;
import com.demo.island.controller.ActorView;
import com.demo.island.controller.GhostController;
import com.demo.island.controller.HumanController;
import com.demo.island.controller.MonkeyController;
import com.demo.island.controller.PlayerAiController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple simulation runner that can use an AI player or console input based on a flag.
 */
@Component
public class SimulationRunner implements CommandLineRunner {

    private static final Logger LOG = Logger.getLogger(SimulationRunner.class.getName());
    private static final Logger SPECTATOR = Logger.getLogger("SPECTATOR");

    private final GameRepository gameRepository;
    private final DmAgent dmAgent;
    private final TurnEngine turnEngine;
    private final PlayerAgent playerAgent;
    private final boolean useAiPlayer;
    private final int turnCap;

    public SimulationRunner(GameRepository gameRepository, DmAgent dmAgent, TurnEngine turnEngine, PlayerAgent playerAgent,
                            @org.springframework.beans.factory.annotation.Value("${player.ai:false}") boolean useAiPlayer,
                            @org.springframework.beans.factory.annotation.Value("${sim.turnCap:100}") int turnCap) {
        this.gameRepository = gameRepository;
        this.dmAgent = dmAgent;
        this.turnEngine = turnEngine;
        this.playerAgent = playerAgent;
        this.useAiPlayer = useAiPlayer;
        this.turnCap = turnCap;
    }

    @Override
    public void run(String... args) {
        LOG.setLevel(Level.INFO);
        SPECTATOR.setLevel(Level.INFO);

        String sessionId = "session-" + UUID.randomUUID();
        WorldState worldState = WorldFactory.createDemoWorld(sessionId);
        gameRepository.createNewSession(worldState);

        this.turnEngine.setAutoProcessCreatures(false);

        Scanner scanner = new Scanner(System.in);
        String playerId = "player-1";
        String ghostId = "ghost";
        String monkeyId = "monkeys";

        ActorController playerController = useAiPlayer
                ? new PlayerAiController(playerAgent, turnEngine, playerId)
                : new HumanController(scanner, playerId);
        ActorController ghostController = new GhostController(ghostId);
        ActorController monkeyController = new MonkeyController(monkeyId);

        System.out.println("Simulation starting. AI player mode: " + useAiPlayer + ". Type commands or let AI drive. QUIT to exit.");

        while (true) {
            int turnStart = worldState.getSession().getTurnNumber();

            ActorIntent playerIntent = playerController.decide(new ActorView(worldState, playerId));
            if (playerIntent == null) {
                break;
            }
            if (playerIntent.getKind() == ActorIntent.Kind.PLAYER_COMMAND) {
                String commandText = playerIntent.getPayload();
                if (commandText == null || commandText.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
                PlayerCommand command = new PlayerCommand(playerId, commandText.trim());
                DmDecision decision = turnEngine.runTurn(worldState, command, dmAgent);
                logSpectator(worldState, commandText, decision);
                gameRepository.save(worldState);
            }

            boolean advanced = worldState.getSession().getTurnNumber() > turnStart;
            if (advanced) {
                ActorIntent ghostIntent = ghostController.decide(new ActorView(worldState, ghostId));
                if (ghostIntent != null && ghostIntent.getKind() == ActorIntent.Kind.GHOST_TURN) {
                    turnEngine.runGhostTurn(worldState);
                }
                ActorIntent monkeyIntent = monkeyController.decide(new ActorView(worldState, monkeyId));
                if (monkeyIntent != null && monkeyIntent.getKind() == ActorIntent.Kind.MONKEY_TURN) {
                    turnEngine.runMonkeyTurn(worldState);
                }
            }

            if (worldState.getSession().getTurnNumber() >= worldState.getSession().getMaxTurns()) {
                System.out.println("RESULT=MIDNIGHT_NO_RAFT (turn cap reached)");
                break;
            }
            if (worldState.getSession().getTurnNumber() >= turnCap) {
                System.out.println("RESULT=SAFETY_STOP (turn cap)");
                break;
            }
        }

        System.out.println("Goodbye.");
    }

    private void logSpectator(WorldState worldState, String commandText, DmDecision decision) {
        int turn = worldState.getSession().getTurnNumber();
        String phase = worldState.getSession().getTimePhase().name();
        boolean ghostAwakened = worldState.getSession().isGhostAwakened();

        // Transcript block
        SPECTATOR.info(() -> """
                Turn %d | Phase=%s | ghostAwakened=%s
                Player: %s
                DM: %s
                """.formatted(turn, phase, ghostAwakened, commandText, decision.getNarration().trim()));

        // Compact summary line
        String ghostSummary = worldState.getCreatures().values().stream()
                .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.GHOST)
                .findFirst()
                .map(c -> "ghostTarget=" + (c.getTargetTileId() == null ? "-" : c.getTargetTileId()))
                .orElse("ghostTarget=-");
        String monkeySummary = worldState.getCreatures().values().stream()
                .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.MONKEY_TROOP)
                .findFirst()
                .map(c -> "monkeyTarget=" + (c.getTargetTileId() == null ? "-" : c.getTargetTileId()))
                .orElse("monkeyTarget=-");
        String checksSummary = decision.getCheckResults().stream()
                .map(cr -> "check=" + cr.getRequest().getType() + ":" + cr.getTotal() + "/" + cr.getRequest().getDifficulty() + (cr.isSuccess() ? ":ok" : ":fail"))
                .findFirst()
                .orElse("");

        System.out.println("ROUND t=%d phase=%s cmd=\"%s\" %s %s %s".formatted(turn, phase, commandText, ghostSummary, monkeySummary, checksSummary));
    }
}
