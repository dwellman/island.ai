package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.dto.ThingContext;
import com.demo.island.world.CharacterThing;
import com.demo.island.world.Direction8;
import com.demo.island.world.ItemThing;
import com.demo.island.world.WorldThingIndex;
import com.demo.island.game.ghost.GhostMode;
import com.demo.island.game.GhostPresenceEvent;
import com.demo.island.game.GhostPresenceTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ToolActionExecutor {

    private final Map<PlayerTool, ToolHandler> handlers = Map.of(
            PlayerTool.LOOK, this::handleLook,
            PlayerTool.SEARCH, this::handleSearch,
            PlayerTool.MOVE, this::handleMove,
            PlayerTool.TAKE, this::handleTake,
            PlayerTool.DROP, this::handleDrop,
            PlayerTool.RAFT_WORK, this::handleRaftWork,
            PlayerTool.STATUS, this::handleStatus
    );

    public ToolOutcome execute(ToolContext ctx) {
        ToolHandler handler = handlers.get(ctx.tool);
        if (handler == null) {
            return blocked("Unsupported tool.", ReasonCode.UNKNOWN, ctx, ctx.action);
        }
        return handler.execute(ctx);
    }

    private ToolOutcome handleLook(ToolContext ctx) {
        if (!isPlayer(ctx)) {
            String body = ctx.plotContext != null && ctx.plotContext.currentDescription != null
                    ? ctx.plotContext.currentDescription
                    : "The area seems quiet.";
            TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                    GameAction.simple(GameActionType.LOOK),
                    body,
                    true,
                    null,
                    null,
                    null,
                    ctx.plotContext != null ? ctx.plotContext.plotId : ctx.actorThing.getCurrentPlotId(),
                    ctx.plotContext);
            return new ToolOutcome(OutcomeType.SUCCESS, ReasonCode.OK, body, turnCtx, null, null);
        }
        GameActionResult res = GameEngine.perform(ctx.session, GameAction.simple(GameActionType.LOOK));
        return outcomeFrom(res, ReasonCode.OK, ctx);
    }

    private ToolOutcome handleSearch(ToolContext ctx) {
        if (!isPlayer(ctx)) {
            String body = "The monkeys forage but find nothing of note.";
            TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                    GameAction.simple(GameActionType.SEARCH),
                    body,
                    true,
                    null,
                    null,
                    null,
                    ctx.plotContext != null ? ctx.plotContext.plotId : ctx.actorThing.getCurrentPlotId(),
                    ctx.plotContext);
            return new ToolOutcome(OutcomeType.SUCCESS, ReasonCode.OK, body, turnCtx, null, null);
        }
        GameActionResult res = GameEngine.perform(ctx.session, GameAction.simple(GameActionType.SEARCH));
        return outcomeFrom(res, ReasonCode.OK, ctx);
    }

    private ToolOutcome handleMove(ToolContext ctx) {
        Direction8 dir = ctx.action.getDirection();
        if (dir == null) {
            return blocked("You need a direction.", ReasonCode.NEEDS_DIRECTION, ctx, ctx.action);
        }
        if (!hasExit(ctx.plotContext, dir)) {
            return blocked("You cannot go that way.", ReasonCode.NO_EXIT_IN_DIRECTION, ctx, ctx.action);
        }
        if (!isPlayer(ctx)) {
            String nextPlotId = nextPlotId(ctx, dir);
            if (nextPlotId == null) {
                return blocked("You cannot go that way.", ReasonCode.NO_EXIT_IN_DIRECTION, ctx, ctx.action);
            }
            ctx.thingIndex.moveThing(ctx.actorId, nextPlotId);
            TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                    GameAction.move(GameActionType.MOVE_WALK, dir),
                    "The troop moves " + dir + ".",
                    true,
                    null,
                    null,
                    null,
                    nextPlotId,
                    ContextBuilder.buildPlotContext(ctx.session, nextPlotId));
            return new ToolOutcome(OutcomeType.SUCCESS, ReasonCode.OK, "The troop moves " + dir + ".", turnCtx, null, null);
        }
        GameActionResult res = GameEngine.perform(ctx.session, ctx.action);
        ReasonCode code = res.isSuccess() ? ReasonCode.OK : ReasonCode.NO_EXIT_IN_DIRECTION;
        return outcomeFrom(res, code, ctx);
    }

    private ToolOutcome handleTake(ToolContext ctx) {
        String target = ctx.targetRaw == null ? "" : ctx.targetRaw;
        if (target.isBlank()) {
            return blocked("Pick up what?", ReasonCode.NEEDS_ITEM, ctx, ctx.action);
        }
        boolean alreadyCarrying = ctx.session.getInventory().getOrDefault(ctx.action.getItemType(), 0) > 0
                || ctx.visibleItems.stream().anyMatch(it -> "THING_PLAYER".equals(it.getCarriedByCharacterId())
                && it.getItemType() == ctx.action.getItemType());
        if (alreadyCarrying) {
            return blocked("You are already carrying that.", ReasonCode.ALREADY_CARRYING_ITEM, ctx, ctx.action);
        }
        boolean visibleHere = ctx.visibleItems.stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(target) || it.getItemType() == ctx.action.getItemType());
        if (!visibleHere) {
            return blocked("You haven't found any items here.", ReasonCode.NO_VISIBLE_ITEMS_HERE, ctx, ctx.action);
        }
        GameActionResult res = GameEngine.perform(ctx.session, ctx.action);
        ReasonCode code = res.isSuccess() ? ReasonCode.OK : ReasonCode.NO_VISIBLE_ITEMS_HERE;
        return outcomeFrom(res, code, ctx);
    }

    private ToolOutcome handleDrop(ToolContext ctx) {
        if (ctx.action.getItemType() == null) {
            return blocked("Drop what?", ReasonCode.NEEDS_ITEM, ctx, ctx.action);
        }
        boolean hasItem = ctx.session.getInventory().getOrDefault(ctx.action.getItemType(), 0) > 0
                || ctx.visibleItems.stream().anyMatch(it -> "THING_PLAYER".equals(it.getCarriedByCharacterId())
                && it.getItemType() == ctx.action.getItemType());
        if (!hasItem) {
            return blocked("You don't have that.", ReasonCode.NOT_CARRYING_ITEM, ctx, ctx.action);
        }
        GameActionResult res = GameEngine.perform(ctx.session, ctx.action);
        ReasonCode code = res.isSuccess() ? ReasonCode.OK : ReasonCode.NOT_CARRYING_ITEM;
        return outcomeFrom(res, code, ctx);
    }

    private ToolOutcome handleRaftWork(ToolContext ctx) {
        GameActionResult res = GameEngine.perform(ctx.session, GameAction.simple(GameActionType.RAFT_WORK_MAJOR));
        ReasonCode code = res.isSuccess() ? ReasonCode.OK : ReasonCode.UNKNOWN;
        return outcomeFrom(res, code, ctx);
    }

    private ToolOutcome handleStatus(ToolContext ctx) {
        String text = ctx.session.getClock().formatRemainingBracketed() + " You check your watch and gear.";
        GhostPresenceEvent ghostEvent = GhostPresenceTracker.maybeTrigger(ctx.session, true).orElse(null);
        if (ghostEvent != null) {
            var manifest = com.demo.island.game.ghost.GhostAgentRegistry.manifest(ctx.session, ghostEvent);
            GhostMode mode = manifest != null && manifest.mode() != null ? manifest.mode() : GhostMode.PRESENCE_ONLY;
            String manifestText = manifest != null ? manifest.text() : "";
            ghostEvent = new GhostPresenceEvent(ghostEvent.plotId(), ghostEvent.eventText(), ghostEvent.reason(), mode, manifestText);
            ctx.session.recordGhostManifest(ghostEvent.plotId(), mode.name(), manifestText);
        }
        TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                GameAction.simple(GameActionType.LOOK),
                "You check your watch and gear.",
                true,
                null,
                null,
                ghostEvent);
        return new ToolOutcome(OutcomeType.SUCCESS, ReasonCode.OK, text, turnCtx, null, null);
    }

    private ToolOutcome blocked(String text, ReasonCode reason, ToolContext ctx, GameAction action) {
        if (!isPlayer(ctx)) {
            TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                    action != null ? action : GameAction.simple(GameActionType.LOOK),
                    text,
                    false,
                    null,
                    null,
                    null,
                    ctx.plotContext != null ? ctx.plotContext.plotId : ctx.actorThing.getCurrentPlotId(),
                    ctx.plotContext);
            return new ToolOutcome(OutcomeType.BLOCKED, reason,
                    text,
                    turnCtx,
                    null,
                    null);
        }
        TurnContext turnCtx = TurnContextBuilder.build(ctx.session,
                action != null ? action : GameAction.simple(GameActionType.LOOK),
                text,
                false,
                null,
                null);
        return new ToolOutcome(OutcomeType.BLOCKED, reason,
                text,
                turnCtx,
                null,
                null);
    }

    private ToolOutcome outcomeFrom(GameActionResult res, ReasonCode code, ToolContext ctx) {
        OutcomeType type = res.isSuccess() ? OutcomeType.SUCCESS : OutcomeType.BLOCKED;
        return new ToolOutcome(type, code, res.getMessage(), res.getTurnContext(), null, null);
    }

    private boolean hasExit(PlotContext plotContext, Direction8 dir) {
        if (plotContext == null || plotContext.neighborSummaries == null) return false;
        return plotContext.neighborSummaries.containsKey(dir);
    }

    private boolean isPlayer(ToolContext ctx) {
        return ctx.actorId == null || "THING_PLAYER".equals(ctx.actorId);
    }

    private String nextPlotId(ToolContext ctx, Direction8 dir) {
        if (ctx.plotContext == null || ctx.plotContext.plotId == null) return null;
        return ctx.session.getMap()
                .get(ctx.session.getMap().get(ctx.plotContext.plotId).orElseThrow().getPosition().step(dir))
                .map(t -> t.getTileId())
                .orElse(null);
    }

    public static ToolContext buildContext(GameSession session,
                                           PlotContext plotContext,
                                           PlayerToolRequest request,
                                           String targetRaw,
                                           String reason,
                                           String mood,
                                           String note) {
        return buildContext(session, plotContext, request, targetRaw, reason, mood, note, "THING_PLAYER");
    }

    public static ToolContext buildContext(GameSession session,
                                           PlotContext plotContext,
                                           PlayerToolRequest request,
                                           String targetRaw,
                                           String reason,
                                           String mood,
                                           String note,
                                           String actorId) {
        WorldThingIndex index = session.getThingIndex();
        CharacterThing actor = (CharacterThing) index.getThing(actorId);
        if (actor == null) {
            actor = (CharacterThing) index.getThing("THING_PLAYER");
        }
        String plotId = plotContext != null ? plotContext.plotId : actor != null ? actor.getCurrentPlotId() : session.getLocation().getTileId();
        if (plotContext == null && plotId != null) {
            plotContext = ContextBuilder.buildPlotContext(session, plotId);
        }
        List<ItemThing> visibleItems = new ArrayList<>();
        if (plotContext != null && plotContext.visibleThings != null) {
            for (ThingContext tc : plotContext.visibleThings) {
                if (tc.getKind() == com.demo.island.world.ThingKind.ITEM) {
                    ItemThing it = (ItemThing) index.getThing(tc.getId());
                    if (it != null) {
                        visibleItems.add(it);
                    }
                }
            }
        }
        GameAction action = mapAction(request);
        return new ToolContext(session, plotContext, request.getTool(), action, targetRaw, reason, mood, note, index, actor, visibleItems, actorId);
    }

    private static GameAction mapAction(PlayerToolRequest req) {
        return switch (req.getTool()) {
            case LOOK -> GameAction.simple(GameActionType.LOOK);
            case SEARCH -> GameAction.simple(GameActionType.SEARCH);
            case MOVE -> GameAction.move(GameActionType.MOVE_WALK, req.getDirection());
            case TAKE -> GameAction.withItem(GameActionType.PICK_UP, req.getItemType());
            case DROP -> GameAction.withItem(GameActionType.DROP, req.getItemType());
            case RAFT_WORK -> GameAction.simple(GameActionType.RAFT_WORK_MAJOR);
            case STATUS -> GameAction.simple(GameActionType.LOOK);
        };
    }
}
