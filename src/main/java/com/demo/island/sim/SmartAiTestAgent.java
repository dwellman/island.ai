package com.demo.island.sim;

import com.demo.island.game.AgentDecision;
import com.demo.island.game.AgentMood;
import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.game.GameItemType;
import com.demo.island.game.GameSession;
import com.demo.island.game.PlayerTool;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.PlayerToolResult;
import com.demo.island.world.Direction8;
import com.demo.island.world.IslandMap;
import com.demo.island.world.IslandTile;
import com.demo.island.world.Position;
import com.demo.island.world.TileKind;
import com.demo.island.world.TileSafety;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A simple heuristic AI that tries to collect raft resources and launch.
 */
public final class SmartAiTestAgent implements ExternalPlayerAgent {

    private static final Map<GameItemType, Integer> REQUIRED_TOTALS = Map.of(
            GameItemType.WOOD_LOG, 5,    // 3 + 2
            GameItemType.VINE_ROPE, 3,   // 2 + 1
            GameItemType.METAL_SCRAP, 1  // final step
    );

    private final Set<String> visitedAnchors = new HashSet<>();
    private final Map<GameItemType, Integer> maxInventorySeen = new EnumMap<>(GameItemType.class);

    public Set<String> getVisitedAnchors() {
        return Set.copyOf(visitedAnchors);
    }

    public Map<GameItemType, Integer> getMaxInventorySeen() {
        return Map.copyOf(maxInventorySeen);
    }

    @Override
    public AgentDecision decideNext(GameSession session, PlayerToolResult lastResult) {
        IslandTile current = session.getMap().get(session.getLocation().getTileId()).orElseThrow();
        if (current.getKind() == TileKind.ANCHOR) {
            visitedAnchors.add(current.getTileId());
        }
        session.getInventory().forEach((k, v) -> maxInventorySeen.put(k, Math.max(maxInventorySeen.getOrDefault(k, 0), v)));

        // Win if ready
        if (session.isRaftReady()) {
            if ("T_WRECK_BEACH".equals(current.getTileId())) {
                return new AgentDecision(PlayerToolRequest.raftWork(),
                        "Raft is ready; stay at the beach to launch soon.",
                        AgentMood.PROGRESSING, "");
            }
            return new AgentDecision(moveToward(session.getMap(), current, "T_WRECK_BEACH"),
                    "Head to the launch site at the beach.", AgentMood.PROGRESSING, "");
        }

        // Work on raft if at build site and have materials
        if ((current.getTileId().equals("T_CAMP") || current.getTileId().equals("T_WRECK_BEACH"))
                && hasMaterialsForNextStep(session)) {
            return new AgentDecision(PlayerToolRequest.raftWork(),
                    "Use gathered materials to advance raft construction.", AgentMood.PROGRESSING, "");
        }

        // If resources present and needed, pick up
        GameItemType neededHere = neededItemHere(session);
        if (neededHere != null) {
            return new AgentDecision(PlayerToolRequest.take(neededHere),
                    "Collect needed resource: " + neededHere, AgentMood.PROGRESSING, "");
        }

        // Discover resources on this plot
        if (!isDiscovered(session)) {
            return new AgentDecision(PlayerToolRequest.search(),
                    "Search the area for usable resources.", AgentMood.CURIOUS, "");
        }

        // Explore: prefer unvisited anchors via greedy move
        Optional<String> targetAnchor = findUnvisitedAnchor(session.getMap());
        if (targetAnchor.isPresent()) {
            return new AgentDecision(moveToward(session.getMap(), current, targetAnchor.get()),
                    "Explore unvisited anchor: " + targetAnchor.get(), AgentMood.CURIOUS, "");
        }

        // Default: move along any safe neighbor
        PlayerToolRequest fallbackMove = moveAnySafe(session.getMap(), current).orElse(PlayerToolRequest.look());
        return new AgentDecision(fallbackMove,
                "Keep moving or gather info to avoid getting stuck.", AgentMood.CAUTIOUS, "");
    }

    private boolean hasMaterialsForNextStep(GameSession session) {
        int progress = session.getRaftProgress();
        if (progress >= 3) return false;
        Map<GameItemType, Integer> inv = session.getInventory();
        int needWood = progress == 0 ? 3 : progress == 1 ? 2 : 0;
        int needRope = progress == 1 ? 2 : progress == 2 ? 1 : 0;
        int needScrap = progress == 2 ? 1 : 0;
        return inv.getOrDefault(GameItemType.WOOD_LOG, 0) >= needWood
                && inv.getOrDefault(GameItemType.VINE_ROPE, 0) >= needRope
                && inv.getOrDefault(GameItemType.METAL_SCRAP, 0) >= needScrap;
    }

    private GameItemType neededItemHere(GameSession session) {
        var res = session.getPlotResources().get(session.getLocation().getTileId());
        if (res == null || !res.isDiscovered()) {
            return null;
        }
        for (var entry : REQUIRED_TOTALS.entrySet()) {
            GameItemType item = entry.getKey();
            int have = session.getInventory().getOrDefault(item, 0);
            if (have < entry.getValue() && res.has(item)) {
                return item;
            }
        }
        return null;
    }

    private boolean isDiscovered(GameSession session) {
        var res = session.getPlotResources().get(session.getLocation().getTileId());
        return res != null && res.isDiscovered();
    }

    private Optional<String> findUnvisitedAnchor(IslandMap map) {
        return map.allTiles().stream()
                .filter(t -> t.getKind() == TileKind.ANCHOR)
                .map(IslandTile::getTileId)
                .filter(id -> !visitedAnchors.contains(id))
                .findFirst();
    }

    private PlayerToolRequest moveToward(IslandMap map, IslandTile current, String targetId) {
        IslandTile target = map.get(targetId).orElse(null);
        if (target == null) {
            return moveAnySafe(map, current).orElse(PlayerToolRequest.look());
        }
        Position cur = current.getPosition();
        Position tgt = target.getPosition();
        Direction8 desired = chooseStep(cur, tgt);
        if (desired != null) {
            IslandTile neighbor = map.get(cur.step(desired)).orElse(null);
            if (neighbor != null && neighbor.getSafety() == TileSafety.NORMAL && neighbor.isWalkable()) {
                return PlayerToolRequest.move(desired);
            }
        }
        return moveAnySafe(map, current).orElse(PlayerToolRequest.look());
    }

    private Optional<PlayerToolRequest> moveAnySafe(IslandMap map, IslandTile current) {
        for (Direction8 dir : Direction8.values()) {
            IslandTile neighbor = map.get(current.getPosition().step(dir)).orElse(null);
            if (neighbor != null && neighbor.getSafety() == TileSafety.NORMAL && neighbor.isWalkable()) {
                return Optional.of(PlayerToolRequest.move(dir));
            }
        }
        return Optional.empty();
    }

    private Direction8 chooseStep(Position cur, Position tgt) {
        int dx = Integer.compare(tgt.x(), cur.x());
        int dy = Integer.compare(tgt.y(), cur.y());
        if (dx == 0 && dy == 0) return null;
        if (dx == 0 && dy > 0) return Direction8.N;
        if (dx == 0 && dy < 0) return Direction8.S;
        if (dx > 0 && dy == 0) return Direction8.E;
        if (dx < 0 && dy == 0) return Direction8.W;
        if (dx > 0 && dy > 0) return Direction8.NE;
        if (dx > 0 && dy < 0) return Direction8.SE;
        if (dx < 0 && dy > 0) return Direction8.NW;
        if (dx < 0 && dy < 0) return Direction8.SW;
        return null;
    }
}
