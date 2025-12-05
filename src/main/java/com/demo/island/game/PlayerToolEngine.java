package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.dto.ThingContext;
import com.demo.island.game.memory.PlayerMemoryRecorder;
import com.demo.island.world.IslandTile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Shim that maps PlayerTool requests to GameEngine actions and returns narrated text plus a lightweight state snapshot.
 */
public final class PlayerToolEngine {

    private static final Logger LOG = LogManager.getLogger(PlayerToolEngine.class);
    private final GameSession session;
    private TurnContext lastTurn;
    private final ToolActionExecutor executor = new ToolActionExecutor();
    private int turnCounter = 0;

    public PlayerToolEngine(GameSession session) {
        this.session = session;
    }

    public PlayerToolResult invoke(PlayerToolRequest request) {
        turnCounter += 1;
        PlotContext plotContext = ContextBuilder.buildPlotContext(session);
        String targetRaw = rawTarget(request);
        ToolContext ctx = ToolActionExecutor.buildContext(session, plotContext, request, targetRaw, "", "", "");
        ToolOutcome outcome = executor.execute(ctx);

        lastTurn = outcome.getTurnContext() != null ? outcome.getTurnContext() : lastTurn;
        PlotContext memoryContext = lastTurn != null ? lastTurn.plotContext : plotContext;
        PlayerMemoryRecorder.recordVisit(session, memoryContext, session.getClock().formatRemainingBracketed());
        String status = switch (outcome.getOutcomeType()) {
            case SUCCESS -> "success";
            case FAIL -> "fail";
            case BLOCKED -> "blocked";
        };
        PlayerToolState state = buildState(request.getTool(), status);

        TurnContext dmContext = lastTurn != null ? lastTurn : TurnContextBuilder.build(session, GameAction.simple(GameActionType.LOOK), "", true, null, null);
        String coreBody = DmMessageMapper.bodyFor(outcome);
        String chosenBody = maybeRewriteDm(dmContext, outcome, request, targetRaw, coreBody);
        String text = DmMessageMapper.messageForBody(chosenBody, session.getClock().formatRemainingBracketed());
        TurnContext resultTurn = lastTurn != null ? lastTurn : dmContext;
        return new PlayerToolResult(text, state, resultTurn);
    }

    private String rawTarget(PlayerToolRequest req) {
        return switch (req.getTool()) {
            case MOVE -> req.getDirection() != null ? req.getDirection().name() : "";
            case TAKE, DROP -> req.getItemType() != null ? req.getItemType().name() : "";
            default -> "";
        };
    }

    private PlayerToolState buildState(PlayerTool tool, String resultStatus) {
        TurnContext ctx = lastTurn != null ? lastTurn : TurnContextBuilder.build(session, GameAction.simple(GameActionType.LOOK), "", true, null, null);

        PlayerToolState state = new PlayerToolState();
        state.time = session.getClock().formatRemainingBracketed();
        state.phase = session.getClock().getPhase().name();
        state.locationId = ctx.currentPlotId;
        state.locationSummary = summarizeLocation(ctx.plotContext);
        state.visibleItems = visibleItems(ctx.plotContext);
        state.visibleExits = exits(ctx.plotContext);
        state.inventory = inventoryList();
        state.raftProgress = session.getRaftProgress();
        state.raftReady = session.isRaftReady();
        state.lastTool = tool;
        state.lastToolResult = resultStatus;
        return state;
    }

    private String summarizeLocation(PlotContext plotContext) {
        if (plotContext == null || plotContext.currentDescription == null) {
            return "Unknown location";
        }
        String desc = plotContext.currentDescription.trim();
        int idx = desc.indexOf('.');
        if (idx > 0) {
            return desc.substring(0, idx + 1).trim();
        }
        return desc;
    }

    private List<String> visibleItems(PlotContext plotContext) {
        if (plotContext == null || plotContext.visibleThings == null) return List.of();
        List<String> items = new ArrayList<>();
        for (ThingContext t : plotContext.visibleThings) {
            if (t.getKind() == com.demo.island.world.ThingKind.ITEM) {
                items.add(t.getName());
            }
        }
        return items;
    }

    private Map<String, String> exits(PlotContext plotContext) {
        if (plotContext == null || plotContext.neighborSummaries == null) return Map.of();
        return plotContext.neighborSummaries.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }

    private List<String> inventoryList() {
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        java.util.Set<GameItemType> itemTypesPresent = new java.util.HashSet<>();
        session.getThingIndex().getAll().values().forEach(t -> {
            if (t instanceof com.demo.island.world.ItemThing it) {
                if ("THING_PLAYER".equals(it.getCarriedByCharacterId())) {
                    names.add(it.getName() == null ? it.getItemType().name() : it.getName());
                    itemTypesPresent.add(it.getItemType());
                }
            }
        });
        if (!names.isEmpty()) {
            return new java.util.ArrayList<>(names);
        }
        session.getInventory().forEach((item, count) -> {
            if (count > 0) {
                if (itemTypesPresent.contains(item)) {
                    return; // already represented by a carried Thing name
                }
                String base = item.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
                if (!names.contains(base) && !names.contains(item.name())) {
                    names.add(count == 1 ? base : base + " x" + count);
                }
            }
        });
        return names.isEmpty() ? List.of() : new java.util.ArrayList<>(names);
    }

    private String maybeRewriteDm(TurnContext ctx,
                                  ToolOutcome outcome,
                                  PlayerToolRequest request,
                                  String targetRaw,
                                  String coreBody) {
        if (!DmAgentConfig.isEnabled()) {
            return coreBody;
        }
        DmAgent agent = DmAgentRegistry.getAgent();
        if (agent == null) {
            return coreBody;
        }
        try {
            DmAgentContext context = buildDmAgentContext(ctx, outcome, request, targetRaw, coreBody);
            String override = agent.rewrite(context);
            if (override != null && !override.isBlank()) {
                LOG.info("DmAgent: override used (tool={}, reasonCode={}).",
                        context.actionOutcome().toolName(), context.actionOutcome().reasonCode());
                return override.trim();
            }
            return coreBody;
        } catch (Exception ex) {
            return coreBody;
        }
    }

    private DmAgentContext buildDmAgentContext(TurnContext ctx,
                                               ToolOutcome outcome,
                                               PlayerToolRequest request,
                                               String targetRaw,
                                               String coreBody) {
        DmAgentPlayerView playerView = new DmAgentPlayerView("Player 1", inventoryList(), null, null, List.of());
        PlotContext plotContext = ctx.plotContext;
        Map<String, String> exits = plotContext != null && plotContext.neighborSummaries != null
                ? plotContext.neighborSummaries.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue))
                : Map.of();
        List<String> visibleItems = plotContext != null && plotContext.visibleThings != null
                ? plotContext.visibleThings.stream()
                .filter(t -> t.getKind() == com.demo.island.world.ThingKind.ITEM)
                .map(ThingContext::getName)
                .toList()
                : List.of();
        String plotId = plotContext != null ? plotContext.plotId : null;
        String plotName = plotId;
        String biome = plotContext != null ? plotContext.biome : null;
        String region = plotContext != null ? plotContext.region : null;
        String description = plotContext != null ? plotContext.currentDescription : null;
        DmAgentPlotView plotView = new DmAgentPlotView(plotId, plotName, biome, region, description, exits, visibleItems);

        String toolName = request.getTool().name();
        DmAgentActionOutcome actionOutcome = new DmAgentActionOutcome(
                toolName,
                targetRaw == null ? "" : targetRaw,
                outcome.getOutcomeType(),
                outcome.getReasonCode(),
                coreBody,
                summarizeChallenge(outcome.getChallenge(), outcome.getChallengeResult()));

        String phase = ctx.phase != null ? ctx.phase.name() : "";
        return new DmAgentContext(turnCounter, ctx.timePrefix, phase, playerView, plotView, actionOutcome, ghostView(ctx));
    }

    private DmAgentGhostView ghostView(TurnContext ctx) {
        if (ctx == null || !ctx.ghostEventTriggered) {
            return new DmAgentGhostView(false, null, null, null, null, null);
        }
        return new DmAgentGhostView(true, ctx.ghostEventPlotId, ctx.ghostEventText, ctx.ghostEventReason, ctx.ghostMode, ctx.ghostText);
    }

    private String summarizeChallenge(Challenge challenge, ChallengeResult result) {
        if (challenge == null || result == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(challenge.getChallengeId()).append(" result=").append(result.isSuccess() ? "success" : "fail");
        sb.append(" roll=").append(result.getTotal());
        return sb.toString();
    }

    public static void setDmAgentForTests(DmAgent agent) {
        DmAgentRegistry.setAgent(agent);
    }

    public static void resetDmAgentForTests() {
        DmAgentRegistry.reset();
    }
}
