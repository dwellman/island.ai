package com.demo.island.engine;

import com.demo.island.core.ActorEvent;
import com.demo.island.core.GameSession;
import com.demo.island.core.Tile;
import com.demo.island.core.TileEvent;
import com.demo.island.core.WorldState;
import com.demo.island.engine.check.CheckService;
import com.demo.island.engine.dice.DiceService;
import com.demo.island.ghost.GhostAgent;
import com.demo.island.ghost.GhostDecision;
import com.demo.island.ghost.GhostInput;
import com.demo.island.ghost.GhostIntent;
import com.demo.island.engine.FlagTarget;
import com.demo.island.engine.SetFlagChange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class TurnEngine {

    private static final Logger LOG = LogManager.getLogger(TurnEngine.class);
    private final DiceService diceService;
    private final CheckService checkService;
    private final java.util.List<com.demo.island.engine.check.CheckResult> recentCheckResults = new java.util.ArrayList<>();
    private final GhostAgent ghostAgent;
    private final com.demo.island.monkey.MonkeyAgent monkeyAgent;
    private boolean autoProcessCreatures = true;
    private final String monkeyHomeTileId = "T_VINES";

    public TurnEngine() {
        this(new DiceService());
    }

    public TurnEngine(DiceService diceService) {
        this(diceService, new CheckService(diceService));
    }

    public TurnEngine(DiceService diceService, CheckService checkService) {
        this(diceService, checkService, null, null);
    }

    public TurnEngine(DiceService diceService, CheckService checkService, GhostAgent ghostAgent, com.demo.island.monkey.MonkeyAgent monkeyAgent) {
        this(diceService, checkService, ghostAgent, monkeyAgent, true);
    }

    public TurnEngine(DiceService diceService, CheckService checkService, GhostAgent ghostAgent,
                      com.demo.island.monkey.MonkeyAgent monkeyAgent, boolean autoProcessCreatures) {
        this.diceService = diceService;
        this.checkService = checkService;
        this.ghostAgent = ghostAgent;
        this.monkeyAgent = monkeyAgent;
        this.autoProcessCreatures = autoProcessCreatures;
    }

    public DmDecision runTurn(WorldState worldState, PlayerCommand command, DmAgent dmAgent) {
        DmInput dmInput = new DmInput(worldState, command, List.copyOf(recentCheckResults));
        recentCheckResults.clear();
        DmDecision dmDecision = dmAgent.decide(dmInput);

        for (StateChange change : dmDecision.getStateChanges()) {
            change.applyTo(worldState);
            LOG.debug("Applied change: {}", change.getClass().getSimpleName());
        }

        if (dmDecision.isTurnConsumesTime()) {
            worldState.getSession().advanceTurn();
            updateTimePhase(worldState.getSession());
            LOG.debug("Turn advanced to {} phase={}", worldState.getSession().getTurnNumber(),
                    worldState.getSession().getTimePhase());
            if (autoProcessCreatures) {
                runGhostTurn(worldState);
                runMonkeyTurn(worldState);
            }
        }

        // Carry over any check results from this decision to the next input
        if (!dmDecision.getCheckResults().isEmpty()) {
            recentCheckResults.addAll(dmDecision.getCheckResults());
        }

        return dmDecision;
    }

    public void updateTimePhase(GameSession session) {
        double progress = (double) session.getTurnNumber() / session.getMaxTurns();
        GameSession.TimePhase newPhase;
        if (progress < 0.5) {
            newPhase = GameSession.TimePhase.LIGHT;
        } else if (progress < 0.8) {
            newPhase = GameSession.TimePhase.DUSK;
        } else {
            newPhase = GameSession.TimePhase.DARK;
        }
        session.setTimePhase(newPhase);
    }

    // Placeholder: ghost behavior will be added later, this logs the hook point.
    public void runGhostTurn(WorldState worldState) {
        if (ghostAgent == null) {
            runGhostStub(worldState);
            return;
        }

        GhostInput input = new GhostInput(worldState, List.copyOf(recentCheckResults));
        GhostDecision decision = ghostAgent.decide(input);
        if (decision == null) {
            return;
        }

        decision.getActions().forEach(intent -> applyGhostIntent(intent, worldState));
        decision.getCheckRequests().forEach(req -> {
            com.demo.island.engine.check.CheckResult result = checkService.evaluate(worldState, req);
            recentCheckResults.add(result);
        });

        if (!decision.getHints().isEmpty() || !decision.getErrors().isEmpty()) {
            LOG.debug("Ghost hints/errors: {} / {}", decision.getHints(), decision.getErrors());
        }
    }

    public void runMonkeyTurn(WorldState worldState) {
        if (monkeyAgent == null) {
            return;
        }
        com.demo.island.core.Creature monkey = worldState.getCreatures().values().stream()
                .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.MONKEY_TROOP)
                .findFirst()
                .orElse(null);
        if (monkey == null) {
            return;
        }

        String homeTileId = worldState.getTiles().containsKey(monkeyHomeTileId) ? monkeyHomeTileId : monkey.getCurrentTileId();
        com.demo.island.monkey.MonkeyInput input = new com.demo.island.monkey.MonkeyInput(worldState, List.copyOf(recentCheckResults));
        com.demo.island.monkey.MonkeyDecision decision = monkeyAgent.decide(input);
        if (decision == null) {
            return;
        }

        if (decision.getDailyPhase() != null) {
            // simple mode recording as flag if needed in future; currently just logged
            LOG.debug("Monkeys mode: {}", decision.getDailyPhase());
        }
        if (decision.getTargetTileId() != null) {
            monkey.setTargetTileId(decision.getTargetTileId());
        } else {
            monkey.setTargetTileId(homeTileId);
        }

        decision.getIntents().forEach(intent -> {
            switch (intent.getIntent()) {
                case FOLLOW_BANANA_CARRIER -> {
                    // intent only; engine could interpret into future movement bias
                }
                case STEAL_BANANA_FROM_PLAYER -> {
                    stealBananaIfPresent(worldState, monkey, intent.getPlayerId());
                }
                case THROW_POO_AT_PLAYER -> applyPooFlag(worldState, monkey, intent.getPlayerId());
                case HELP_VINE_HARVEST, RETURN_HOME_AND_SLEEP, IGNORE_PLAYERS -> {
                    monkey.setTargetTileId(homeTileId);
                    new SetFlagChange(FlagTarget.CREATURE, monkey.getCreatureId(), "sleeping", true).applyTo(worldState);
                }
                default -> {
                }
            }
        });

        // Ensure monkeys always have a target; default to home if none set by intents.
        if (monkey.getTargetTileId() == null) {
            monkey.setTargetTileId(homeTileId);
        }

        decision.getCheckRequests().forEach(req -> {
            com.demo.island.engine.check.CheckResult result = checkService.evaluate(worldState, req);
            recentCheckResults.add(result);
        });

        if (!decision.getHints().isEmpty() || !decision.getErrors().isEmpty()) {
            LOG.debug("Monkey hints/errors: {} / {}", decision.getHints(), decision.getErrors());
        }
    }

    private String firstPlayerTile(WorldState worldState) {
        return worldState.getPlayers().values().stream()
                .findFirst()
                .map(com.demo.island.core.Player::getCurrentTileId)
                .orElse(null);
    }

    private void runGhostStub(WorldState worldState) {
        GameSession.TimePhase phase = worldState.getSession().getTimePhase();
        if (phase == GameSession.TimePhase.LIGHT) {
            LOG.debug("Ghost idle/wandering during LIGHT phase.");
            return;
        }

        worldState.getCreatures().values().stream()
                .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.GHOST)
                .findFirst()
                .ifPresent(ghost -> {
                    com.demo.island.engine.check.CheckRequest req = new com.demo.island.engine.check.CheckRequest(
                            com.demo.island.engine.check.CheckType.HEARING,
                            com.demo.island.engine.check.CheckSubjectKind.CREATURE,
                            ghost.getCreatureId(),
                            10
                    );
                    com.demo.island.engine.check.CheckResult result = checkService.evaluate(worldState, req);
                    recentCheckResults.add(result);
                    if (result.isSuccess()) {
                        String targetTile = firstPlayerTile(worldState);
                        ghost.setTargetTileId(targetTile);
                        LOG.debug("Ghost heard activity. Target tile set to {}", targetTile);
                        logActorEvent(worldState, ghost.getCreatureId(), targetTile,
                                "GHOST_HEARD_SHOUT", null,
                                "Ghost heard activity near " + targetTile);
                    } else {
                        LOG.debug("Ghost heard nothing. Keeps current target.");
                    }
                });
    }

    private void applyGhostIntent(GhostIntent intent, WorldState worldState) {
        switch (intent.getVerb()) {
            case SET_TARGET_TILE -> {
                com.demo.island.core.Creature creature = worldState.getCreature(intent.getCreatureId());
                if (creature != null) {
                    creature.setTargetTileId(intent.getTargetTileId());
                    LOG.debug("Ghost sets target tile to {}", intent.getTargetTileId());
                }
            }
            case CHECK -> {
                com.demo.island.engine.check.CheckType type = com.demo.island.engine.check.CheckType.valueOf(
                        intent.getCheckType() != null ? intent.getCheckType() : com.demo.island.engine.check.CheckType.HEARING.name()
                );
                com.demo.island.engine.check.CheckSubjectKind subjectKind = intent.getCheckSubjectKind() != null
                        ? com.demo.island.engine.check.CheckSubjectKind.valueOf(intent.getCheckSubjectKind())
                        : com.demo.island.engine.check.CheckSubjectKind.CREATURE;
                String subjectId = intent.getCheckSubjectId() != null
                        ? intent.getCheckSubjectId()
                        : worldState.getCreatures().values().stream()
                        .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.GHOST)
                        .map(com.demo.island.core.Creature::getCreatureId)
                        .findFirst()
                        .orElse(null);
                int dc = intent.getDifficulty() != null ? intent.getDifficulty() : 10;
                com.demo.island.engine.check.CheckRequest req = new com.demo.island.engine.check.CheckRequest(
                        type, subjectKind, subjectId, dc
                );
                com.demo.island.engine.check.CheckResult result = checkService.evaluate(worldState, req);
                recentCheckResults.add(result);
                if (type == com.demo.island.engine.check.CheckType.HEARING && result.isSuccess()) {
                    String targetTile = firstPlayerTile(worldState);
                    logActorEvent(worldState, subjectId, targetTile,
                            "GHOST_HEARD_SHOUT", null,
                            "Ghost heard activity near " + targetTile);
                }
            }
            case SET_FLAG -> {
                if (intent.getFlagTarget() != null && intent.getFlagName() != null && intent.getFlagValue() != null) {
                    new SetFlagChange(
                            FlagTarget.valueOf(intent.getFlagTarget()),
                            null,
                            intent.getFlagName(),
                            intent.getFlagValue()
                    ).applyTo(worldState);
                }
            }
            case TRANSFER_ITEM, RUN_ITEM_HOOK -> {
                // not used in v1 ghost
            }
            default -> {
            }
        }
    }

    private void stealBananaIfPresent(WorldState worldState, com.demo.island.core.Creature monkey, String playerId) {
        if (playerId == null) {
            return;
        }
        com.demo.island.core.Player player = worldState.getPlayer(playerId);
        if (player == null || !monkey.getCurrentTileId().equals(player.getCurrentTileId())) {
            return;
        }
        player.getInventoryItemIds().stream()
                .map(worldState::getItem)
                .filter(item -> item != null && hasTag(worldState, item, "bait"))
                .findFirst()
                .ifPresent(item -> {
                    new TransferItemChange(
                            item.getItemId(),
                            com.demo.island.core.ItemInstance.OwnerKind.CREATURE,
                            monkey.getCreatureId()
                    ).applyTo(worldState);
                    monkey.getCarriedItemIds().add(item.getItemId());
                    player.removeItem(item.getItemId());
                    logTileEvent(worldState, monkey.getCurrentTileId(), monkey.getCreatureId(),
                            "MONKEY_STEAL_BANANA",
                            "Monkeys snatched a banana from " + player.getName() + ".");
                    logActorEvent(worldState, monkey.getCreatureId(), monkey.getCurrentTileId(),
                            "MONKEY_STOLE_BANANA", playerId,
                            "Stole a banana from " + player.getName() + ".");
                    logActorEvent(worldState, playerId, monkey.getCurrentTileId(),
                            "PLAYER_LOST_BANANA", monkey.getCreatureId(),
                            "You lost a banana to the monkeys.");
                });
    }

    private boolean hasTag(WorldState worldState, com.demo.island.core.ItemInstance item, String tag) {
        return java.util.Optional.ofNullable(worldState.getItemType(item.getItemTypeId()))
                .map(type -> type.getTags().contains(tag))
                .orElse(false);
    }

    private void applyPooFlag(WorldState worldState, com.demo.island.core.Creature monkey, String playerId) {
        if (playerId == null || monkey == null) {
            return;
        }
        com.demo.island.core.Player player = worldState.getPlayer(playerId);
        if (player == null || !monkey.getCurrentTileId().equals(player.getCurrentTileId())) {
            return;
        }
        new SetFlagChange(FlagTarget.PLAYER, playerId, "POOED", true).applyTo(worldState);
        player.getStats().set("POOED", 1);
        logTileEvent(worldState, monkey.getCurrentTileId(), monkey.getCreatureId(),
                "MONKEY_THROW_POO",
                "Monkeys hurled poo at " + player.getName() + ".");
        logActorEvent(worldState, monkey.getCreatureId(), monkey.getCurrentTileId(),
                "MONKEY_THROW_POO_AT_PLAYER", playerId,
                "Threw poo at " + player.getName() + ".");
        logActorEvent(worldState, playerId, monkey.getCurrentTileId(),
                "PLAYER_PELTED_WITH_POO", monkey.getCreatureId(),
                "You were pelted with poo by the monkeys.");
    }

    private void logTileEvent(WorldState worldState, String tileId, String actorId, String eventType, String summary) {
        if (worldState == null || tileId == null || eventType == null) {
            return;
        }
        Tile tile = worldState.getTile(tileId);
        if (tile == null) {
            return;
        }
        tile.recordEvent(new TileEvent(
                tileId,
                worldState.getSession().getTurnNumber(),
                actorId,
                eventType,
                summary == null ? "" : summary
        ));
    }

    private void logActorEvent(WorldState worldState, String actorId, String tileId, String eventType, String otherActorId, String summary) {
        if (worldState == null || actorId == null || eventType == null) {
            return;
        }
        com.demo.island.core.Player player = worldState.getPlayer(actorId);
        if (player != null) {
            player.recordEvent(new com.demo.island.core.ActorEvent(
                    worldState.getSession().getTurnNumber(),
                    tileId,
                    eventType,
                    otherActorId,
                    summary == null ? "" : summary
            ));
            return;
        }
        com.demo.island.core.Creature creature = worldState.getCreature(actorId);
        if (creature != null) {
            creature.recordEvent(new com.demo.island.core.ActorEvent(
                    worldState.getSession().getTurnNumber(),
                    tileId,
                    eventType,
                    otherActorId,
                    summary == null ? "" : summary
            ));
        }
    }

    public DiceService getDiceService() {
        return diceService;
    }

    public CheckService getCheckService() {
        return checkService;
    }

    public void setAutoProcessCreatures(boolean autoProcessCreatures) {
        this.autoProcessCreatures = autoProcessCreatures;
    }

    public boolean isAutoProcessCreatures() {
        return autoProcessCreatures;
    }

    public List<com.demo.island.engine.check.CheckResult> getRecentCheckResults() {
        return List.copyOf(recentCheckResults);
    }
}
