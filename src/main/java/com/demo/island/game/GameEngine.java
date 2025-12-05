package com.demo.island.game;

import com.demo.island.world.Direction8;
import com.demo.island.world.GameOverReason;
import com.demo.island.world.IslandTile;
import com.demo.island.world.Position;
import com.demo.island.world.TerrainDifficulty;
import com.demo.island.world.TileSafety;
import com.demo.island.world.WorldGeometry;
import com.demo.island.world.WorldThingIndex;
import com.demo.island.world.ItemThing;
import com.demo.island.world.CharacterThing;
import com.demo.island.world.Thing;
import com.demo.island.game.ghost.GhostAgentRegistry;
import com.demo.island.game.ghost.GhostManifestation;
import com.demo.island.game.ghost.GhostMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class GameEngine {

    private static final Logger LOG = LogManager.getLogger(GameEngine.class);
    private static ChallengeResolver challengeResolver = new ChallengeResolver(new DiceService());
    private static DmAdapter dmAdapter = new DefaultDmAdapter();

    private GameEngine() {
    }

    static void setChallengeResolverForTests(ChallengeResolver resolver) {
        challengeResolver = resolver;
    }

    static void setDmAdapterForTests(DmAdapter adapter) {
        dmAdapter = adapter;
    }

    public static String buildIntroMessage(CosmosClock clock) {
        return clock.formatRemainingBracketed() + " You are standing in the dark just before dawn. You have no idea how you got here.";
    }

    public static GameActionResult perform(GameSession session, GameAction action) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            TurnContext ctx = TurnContextBuilder.build(session, action, "Game is over.", false, null, null, null);
            String body = dmAdapter.narrate(ctx);
            return new GameActionResult(false, prefix(session) + " " + body, ctx);
        }

        ActionOutcome outcome;
        GameActionType type = action.getType();
        switch (type) {
            case MOVE_WALK, MOVE_RUN -> outcome = handleMove(session, action);
            case JUMP -> outcome = handleJump(session, action);
            case LOOK -> {
                applyTime(session, GameActionCost.timeCost(GameActionType.LOOK, currentDiff(session)));
                checkOutOfTime(session);
                outcome = new ActionOutcome(true, "You take a quick look around.", null, null);
            }
            case SEARCH -> {
                applyTime(session, GameActionCost.timeCost(GameActionType.SEARCH, currentDiff(session)));
                discover(session);
                MonkeyPooOutcome poo = maybeMonkeyPoo(session);
                checkOutOfTime(session);
                Challenge challenge = poo.triggered ? poo.challenge : null;
                ChallengeResult challengeResult = poo.triggered ? poo.result : null;
                outcome = new ActionOutcome(true, "You search the area.", challenge, challengeResult);
            }
            case PICK_UP -> outcome = handlePickUp(session, action);
            case DROP -> outcome = handleDrop(session, action);
            case RAFT_WORK_SMALL -> {
                applyTime(session, GameActionCost.timeCost(GameActionType.RAFT_WORK_SMALL, currentDiff(session)));
                checkOutOfTime(session);
                outcome = new ActionOutcome(true, "You tidy some rope and driftwood.", null, null);
            }
            case RAFT_WORK_MAJOR -> outcome = handleRaftWork(session);
            case LAUNCH_RAFT -> outcome = handleLaunch(session);
            default -> outcome = new ActionOutcome(false, "Action not supported.", null, null);
        }

        GhostPresenceEvent ghostEvent = GhostPresenceTracker.maybeTrigger(session, outcome.success).orElse(null);
        if (ghostEvent != null) {
            GhostManifestation manifest = GhostAgentRegistry.manifest(session, ghostEvent);
            GhostMode mode = manifest != null && manifest.mode() != null ? manifest.mode() : GhostMode.PRESENCE_ONLY;
            String text = manifest != null ? manifest.text() : "";
            ghostEvent = new GhostPresenceEvent(ghostEvent.plotId(), ghostEvent.eventText(), ghostEvent.reason(), mode, text);
            session.recordGhostManifest(ghostEvent.plotId(), mode.name(), text);
        }
        TurnContext ctx = TurnContextBuilder.build(session, action, outcome.resultSummary, outcome.success, outcome.challenge, outcome.challengeResult, ghostEvent);
        String body = dmAdapter.narrate(ctx);
        String message = prefix(session) + " " + body;
        return new GameActionResult(outcome.success, message, ctx);
    }

    private static ActionOutcome handleMove(GameSession session, GameAction action) {
        Direction8 dir = action.getDirection();
        if (dir == null) {
            return new ActionOutcome(false, "No direction provided.", null, null);
        }
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new ActionOutcome(false, "You seem to be nowhere.", null, null);
        }
        Position targetPos = current.getPosition().step(dir);
        IslandTile target = session.getMap().get(targetPos).orElse(null);
        if (target == null) {
            return new ActionOutcome(false, "No path that way.", null, null);
        }
        WorldGeometry.Classification cls = WorldGeometry.classify(targetPos);
        if (cls == WorldGeometry.Classification.OFF_WORLD || cls == WorldGeometry.Classification.BOUNDARY) {
            return new ActionOutcome(false, "The island ends that way.", null, null);
        }
        if (target.getSafety() == TileSafety.IMPOSSIBLE) {
            return new ActionOutcome(false, "Impassable terrain.", null, null);
        }
        if (target.getSafety() == TileSafety.DEAD) {
            return new ActionOutcome(false, "That way looks deadly.", null, null);
        }
        int timeCost = GameActionCost.timeCost(action.getType(), target.getDifficulty());
        session.setLocation(new com.demo.island.world.PlayerLocation(target.getTileId()));
        applyTime(session, timeCost);
        MonkeyPooOutcome poo = maybeMonkeyPoo(session);
        checkOutOfTime(session);
        LOG.debug("Moved {} to {} cost={} totalPips={}", dir, target.getTileId(), timeCost, session.getClock().getTotalPips());
        Challenge challenge = poo.triggered ? poo.challenge : null;
        ChallengeResult challengeResult = poo.triggered ? poo.result : null;
        return new ActionOutcome(true, "You move " + dir + ".", challenge, challengeResult);
    }

    private static ActionOutcome handlePickUp(GameSession session, GameAction action) {
        GameItemType item = action.getItemType();
        if (item == null) {
            return new ActionOutcome(false, "Pick up what?", null, null);
        }
        String playerId = "THING_PLAYER";
        WorldThingIndex index = session.getThingIndex();
        if (isItemCarriedByPlayer(index, item, playerId) || session.getInventory().getOrDefault(item, 0) > 0) {
            return new ActionOutcome(false, "You are already carrying that.", null, null);
        }

        ItemThing thing = findItemInPlot(index, session.getLocation().getTileId(), item);
        if (thing != null) {
            index.moveThing(thing.getId(), null);
            thing.setCarriedByCharacterId(playerId);
            session.getInventory().put(item, session.getInventory().getOrDefault(item, 0) + 1);
            applyTime(session, GameActionCost.timeCost(GameActionType.PICK_UP, currentDiff(session)));
            checkOutOfTime(session);
            String name = thing.getName() == null ? item.name().toLowerCase(Locale.ROOT) : thing.getName();
            return new ActionOutcome(true, "You pick up the " + name + ".", null, null);
        }

        // Fallback to legacy plot resources (discovered caches)
        PlotResources res = session.getPlotResources().get(session.getLocation().getTileId());
        if (res == null || !res.isDiscovered()) {
            return new ActionOutcome(false, "You haven't found any items here.", null, null);
        }
        if (!res.has(item)) {
            return new ActionOutcome(false, "You haven't found any items here.", null, null);
        }
        res.take(item);
        session.getInventory().put(item, session.getInventory().getOrDefault(item, 0) + 1);
        applyTime(session, GameActionCost.timeCost(GameActionType.PICK_UP, currentDiff(session)));
        checkOutOfTime(session);
        return new ActionOutcome(true, "You pick up the " + item.name().toLowerCase(Locale.ROOT).replace('_', ' '), null, null);
    }

    private static ActionOutcome handleDrop(GameSession session, GameAction action) {
        GameItemType item = action.getItemType();
        if (item == null) {
            return new ActionOutcome(false, "Drop what?", null, null);
        }
        String playerId = "THING_PLAYER";
        int invCount = session.getInventory().getOrDefault(item, 0);
        ItemThing carried = findCarriedItem(session.getThingIndex(), item, playerId);
        if (invCount <= 0 && carried == null) {
            return new ActionOutcome(false, "You don't have that.", null, null);
        }
        if (invCount > 0) {
            session.getInventory().put(item, Math.max(0, invCount - 1));
        }
        if (carried != null) {
            session.getThingIndex().moveThing(carried.getId(), session.getLocation().getTileId());
            carried.setCarriedByCharacterId(null);
        } else {
            PlotResources res = session.getPlotResources().computeIfAbsent(session.getLocation().getTileId(), k -> new PlotResources(true));
            res.drop(item);
        }
        applyTime(session, GameActionCost.timeCost(GameActionType.DROP, currentDiff(session)));
        checkOutOfTime(session);
        String name = carried != null && carried.getName() != null
                ? carried.getName()
                : item.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return new ActionOutcome(true, "You drop the " + name + ".", null, null);
    }

    private static ActionOutcome handleJump(GameSession session, GameAction action) {
        Direction8 dir = action.getDirection();
        if (dir == null) {
            return new ActionOutcome(false, "You need to say which way to jump.", null, null);
        }
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new ActionOutcome(false, "You seem to be nowhere.", null, null);
        }
        Position targetPos = current.getPosition().step(dir);
        IslandTile target = session.getMap().get(targetPos).orElse(null);
        if (target == null) {
            return new ActionOutcome(false, "There's nowhere to land that way.", null, null);
        }
        WorldGeometry.Classification cls = WorldGeometry.classify(targetPos);
        if (cls == WorldGeometry.Classification.OFF_WORLD || cls == WorldGeometry.Classification.BOUNDARY) {
            return new ActionOutcome(false, "That's beyond the island's edge.", null, null);
        }
        if (target.getSafety() != TileSafety.NORMAL) {
            return new ActionOutcome(false, "That way looks unsafe to jump.", null, null);
        }

        Thing playerThing = session.getThingIndex().getThing("THING_PLAYER");
        boolean prof = false;
        if (playerThing instanceof CharacterThing ct) {
            prof = ct.getSkillProficiencies().contains(Skill.ACROBATICS);
        }
        Challenge challenge = new Challenge(
                "JUMP_GENERIC",
                ChallengeType.SKILL_CHECK,
                Ability.DEX,
                Skill.ACROBATICS,
                12,
                prof,
                "Jumping across a gap or obstacle."
        );
        ChallengeResult result = challengeResolver.resolve((CharacterThing) playerThing, challenge);

        int walkCost = GameActionCost.timeCost(GameActionType.MOVE_WALK, target.getDifficulty());
        int timeCost;
        String summary;
        if (result.isSuccess()) {
            session.setLocation(new com.demo.island.world.PlayerLocation(target.getTileId()));
            timeCost = walkCost;
            summary = "You gather yourself and clear the gap, landing on the far side.";
        } else {
            timeCost = walkCost + 2;
            if (result.isNatural1()) {
                timeCost += 2;
            }
            summary = "You push off, but come up short and scramble back to where you started.";
        }
        applyTime(session, timeCost);
        MonkeyPooOutcome poo = maybeMonkeyPoo(session);
        checkOutOfTime(session);
        Challenge finalChallenge = poo.triggered ? poo.challenge : challenge;
        ChallengeResult finalResult = poo.triggered ? poo.result : result;
        return new ActionOutcome(result.isSuccess(), summary, finalChallenge, finalResult);
    }

    private static ActionOutcome handleRaftWork(GameSession session) {
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new ActionOutcome(false, "Unknown location.", null, null);
        }
        String id = current.getTileId();
        if (!id.equals("T_CAMP") && !id.equals("T_WRECK_BEACH")) {
            return new ActionOutcome(false, "You need a stable site to work on the raft.", null, null);
        }
        int progress = session.getRaftProgress();
        Map<GameItemType, Integer> need = requirementsFor(progress);
        if (need.isEmpty()) {
            return new ActionOutcome(false, "Raft is already complete.", null, null);
        }
        if (!hasInventory(session, need)) {
            return new ActionOutcome(false, "You lack materials: " + need, null, null);
        }
        consumeInventory(session, need);
        session.incrementRaftProgress();
        applyTime(session, GameActionCost.timeCost(GameActionType.RAFT_WORK_MAJOR, currentDiff(session)));
        checkOutOfTime(session);
        return new ActionOutcome(true, "Raft work advanced to step " + session.getRaftProgress(), null, null);
    }

    private static ActionOutcome handleLaunch(GameSession session) {
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        if (current == null) {
            return new ActionOutcome(false, "Unknown location.", null, null);
        }
        if (!current.getTileId().equals("T_WRECK_BEACH")) {
            return new ActionOutcome(false, "You need to launch at the beach.", null, null);
        }
        if (!session.isRaftReady()) {
            return new ActionOutcome(false, "The raft is not ready.", null, null);
        }
        if (session.getClock().isOutOfTime()) {
            return new ActionOutcome(false, "Too late to launch.", null, null);
        }
        applyTime(session, 5);
        session.setStatus(GameStatus.WON);
        session.setGameEndReason(GameEndReason.RAFT_LAUNCHED);
        return new ActionOutcome(true, "You push off and launch the raft!", null, null);
    }

    private static ItemThing findItemInPlot(WorldThingIndex index, String plotId, GameItemType type) {
        if (plotId == null || type == null) return null;
        return index.getThingsInPlot(plotId).stream()
                .filter(ItemThing.class::isInstance)
                .map(ItemThing.class::cast)
                .filter(it -> it.getItemType() == type)
                .filter(it -> it.getCarriedByCharacterId() == null)
                .findFirst()
                .orElse(null);
    }

    private static ItemThing findCarriedItem(WorldThingIndex index, GameItemType type, String playerId) {
        if (type == null) return null;
        return index.getAll().values().stream()
                .filter(ItemThing.class::isInstance)
                .map(ItemThing.class::cast)
                .filter(it -> it.getItemType() == type)
                .filter(it -> playerId.equals(it.getCarriedByCharacterId()))
                .findFirst()
                .orElse(null);
    }

    private static boolean isItemCarriedByPlayer(WorldThingIndex index, GameItemType type, String playerId) {
        return findCarriedItem(index, type, playerId) != null;
    }

    private static MonkeyPooOutcome maybeMonkeyPoo(GameSession session) {
        if (!isInMonkeyTerritory(session)) {
            return MonkeyPooOutcome.notTriggered();
        }
        Thing playerThing = session.getThingIndex().getThing("THING_PLAYER");
        if (!(playerThing instanceof CharacterThing ct)) {
            return MonkeyPooOutcome.notTriggered();
        }
        boolean prof = ct.getSaveProficiencies().contains(Ability.DEX);
        Challenge challenge = new Challenge(
                "DODGE_MONKEY_POO",
                ChallengeType.SAVING_THROW,
                Ability.DEX,
                null,
                12,
                prof,
                "Dodging a volley of monkey-thrown poo."
        );
        ChallengeResult result = challengeResolver.resolve(ct, challenge);

        if (result.isSuccess()) {
            return new MonkeyPooOutcome(true, challenge, result);
        }

        int penalty = 2;
        if (result.isNatural1()) {
            penalty += 3;
        }
        applyTime(session, penalty);
        return new MonkeyPooOutcome(true, challenge, result);
    }

    private static boolean isInMonkeyTerritory(GameSession session) {
        WorldThingIndex index = session.getThingIndex();
        String tileId = session.getLocation().getTileId();
        IslandTile tile = session.getMap().get(tileId).orElse(null);
        if (tile == null) {
            return false;
        }
        for (String tid : tile.getThingsPresent()) {
            Thing t = index.getThing(tid);
            if (t != null && t.getTags().contains("MONKEY_TROOP")) {
                return true;
            }
        }
        for (String tid : tile.getThingsAnchoredHere()) {
            Thing t = index.getThing(tid);
            if (t != null && t.getTags().contains("MONKEY_TROOP")) {
                return true;
            }
        }
        return "T_VINE_FOREST".equals(tileId);
    }

    private static Map<GameItemType, Integer> requirementsFor(int progress) {
        Map<GameItemType, Integer> req = new EnumMap<>(GameItemType.class);
        switch (progress) {
            case 0 -> req.put(GameItemType.WOOD_LOG, 3);
            case 1 -> {
                req.put(GameItemType.WOOD_LOG, 2);
                req.put(GameItemType.VINE_ROPE, 2);
            }
            case 2 -> {
                req.put(GameItemType.METAL_SCRAP, 1);
                req.put(GameItemType.VINE_ROPE, 1);
            }
            default -> {
            }
        }
        return req;
    }

    private static boolean hasInventory(GameSession session, Map<GameItemType, Integer> need) {
        for (var entry : need.entrySet()) {
            if (session.getInventory().getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeInventory(GameSession session, Map<GameItemType, Integer> need) {
        need.forEach((k, v) -> session.getInventory().put(k, session.getInventory().getOrDefault(k, 0) - v));
    }

    private static void discover(GameSession session) {
        PlotResources res = session.getPlotResources().get(session.getLocation().getTileId());
        if (res != null) {
            res.setDiscovered(true);
        }
    }

    private static TerrainDifficulty currentDiff(GameSession session) {
        return session.getMap().get(session.getLocation().getTileId()).map(IslandTile::getDifficulty).orElse(TerrainDifficulty.NORMAL);
    }

    private static void applyTime(GameSession session, int pips) {
        if (pips > 0) {
            session.getClock().advance(pips);
        }
    }

    private static void checkOutOfTime(GameSession session) {
        if (session.getClock().isOutOfTime() && session.getStatus() != GameStatus.WON) {
            session.setStatus(GameStatus.LOST);
            session.setGameEndReason(GameEndReason.OUT_OF_TIME);
        }
    }

    private static String prefix(GameSession session) {
        return session.getClock().formatRemainingBracketed();
    }

    private record ActionOutcome(boolean success, String resultSummary, Challenge challenge, ChallengeResult challengeResult) {
    }

    private static final class MonkeyPooOutcome {
        final boolean triggered;
        final Challenge challenge;
        final ChallengeResult result;

        MonkeyPooOutcome(boolean triggered, Challenge challenge, ChallengeResult result) {
            this.triggered = triggered;
            this.challenge = challenge;
            this.result = result;
        }

        static MonkeyPooOutcome notTriggered() {
            return new MonkeyPooOutcome(false, null, null);
        }
    }
}
