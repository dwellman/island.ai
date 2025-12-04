package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.dto.ThingContext;
import com.demo.island.world.Direction8;
import com.demo.island.world.IslandTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Shim that maps PlayerTool requests to GameEngine actions and returns narrated text plus a lightweight state snapshot.
 */
public final class PlayerToolEngine {

    private final GameSession session;
    private TurnContext lastTurn;

    public PlayerToolEngine(GameSession session) {
        this.session = session;
    }

    public PlayerToolResult invoke(PlayerToolRequest request) {
        PlayerTool tool = request.getTool();
        switch (tool) {
            case STATUS -> {
                PlayerToolState state = buildState(tool, "info");
                String text = session.getClock().formatRemainingBracketed() + " You check your watch and gear.";
                return new PlayerToolResult(text, state);
            }
            case LOOK, MOVE, SEARCH, TAKE, DROP, RAFT_WORK -> {
                Optional<GameAction> actionOpt = mapToAction(request);
                if (actionOpt.isEmpty()) {
                    PlayerToolState state = buildState(tool, "blocked");
                    String text = session.getClock().formatRemainingBracketed() + " That tool needs more info.";
                    return new PlayerToolResult(text, state);
                }
                GameActionResult result = GameEngine.perform(session, actionOpt.get());
                lastTurn = result.getTurnContext();
                String status = result.isSuccess() ? "success" : "blocked";
                PlayerToolState state = buildState(tool, status);
                return new PlayerToolResult(result.getMessage(), state);
            }
            default -> {
                PlayerToolState state = buildState(tool, "blocked");
                String text = session.getClock().formatRemainingBracketed() + " Unsupported tool.";
                return new PlayerToolResult(text, state);
            }
        }
    }

    private Optional<GameAction> mapToAction(PlayerToolRequest req) {
        return switch (req.getTool()) {
            case LOOK -> Optional.of(GameAction.simple(GameActionType.LOOK));
            case MOVE -> req.getDirection() != null
                    ? Optional.of(GameAction.move(GameActionType.MOVE_WALK, req.getDirection()))
                    : Optional.empty();
            case SEARCH -> Optional.of(GameAction.simple(GameActionType.SEARCH));
            case TAKE -> req.getItemType() != null
                    ? Optional.of(GameAction.withItem(GameActionType.PICK_UP, req.getItemType()))
                    : Optional.empty();
            case DROP -> req.getItemType() != null
                    ? Optional.of(GameAction.withItem(GameActionType.DROP, req.getItemType()))
                    : Optional.empty();
            case RAFT_WORK -> Optional.of(GameAction.simple(GameActionType.RAFT_WORK_MAJOR));
            case STATUS -> Optional.empty();
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
        StringJoiner joiner = new StringJoiner(", ");
        session.getInventory().forEach((item, count) -> {
            if (count > 0) {
                if (count == 1) {
                    joiner.add(item.name());
                } else {
                    joiner.add(item.name() + " x" + count);
                }
            }
        });
        String inv = joiner.toString();
        if (inv.isEmpty()) {
            return List.of();
        }
        return List.of(inv.split(", "));
    }
}
