package com.demo.island.game.ghost;

import com.demo.island.game.GameSession;
import com.demo.island.game.GhostPresenceEvent;
import com.demo.island.game.GhostPresenceTracker;
import com.demo.island.world.IslandTile;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the current GhostAgent and invokes it when enabled.
 */
public final class GhostAgentRegistry {

    private static final Logger LOG = LogManager.getLogger(GhostAgentRegistry.class);
    private static GhostAgent agent;
    private static Boolean enabledOverride;

    private GhostAgentRegistry() {
    }

    public static GhostManifestation manifest(GameSession session, GhostPresenceEvent event) {
        if (session == null || event == null) {
            return GhostManifestation.silent();
        }
        if (!isEnabled() || agent == null) {
            return GhostManifestation.silent();
        }
        GhostState state = buildState(session, event);
        try {
            GhostManifestation result = agent.manifest(state);
            if (result == null) {
                return GhostManifestation.silent();
            }
            LOG.info("GhostAgent: mode={} textLen={}", result.mode(), (result.text() == null ? 0 : result.text().length()));
            return result;
        } catch (Exception ex) {
            LOG.info("GhostAgent: failed; using silent. err={}", ex.getMessage());
            return GhostManifestation.silent();
        }
    }

    public static void setAgent(GhostAgent ghostAgent) {
        agent = ghostAgent;
    }

    public static void reset() {
        agent = null;
        enabledOverride = null;
    }

    public static void setEnabledOverride(Boolean enabled) {
        enabledOverride = enabled;
    }

    public static boolean isEnabled() {
        if (enabledOverride != null) {
            return enabledOverride;
        }
        String prop = System.getProperty("ghost.agent.llm.enabled");
        if (prop == null || prop.isBlank()) {
            prop = System.getenv("GHOST_AGENT_LLM_ENABLED");
        }
        return prop != null && Boolean.parseBoolean(prop);
    }

    private static GhostState buildState(GameSession session, GhostPresenceEvent event) {
        int turn = session.getClock().getTotalPips();
        String time = session.getClock().formatRemainingBracketed();
        String phase = session.getClock().getPhase().name();

        IslandTile tile = session.getMap().get(session.getLocation().getTileId()).orElse(null);
        String coords = tile != null ? "(" + tile.getPosition().x() + ", " + tile.getPosition().y() + ")" : "(?, ?)";
        String plotName = tile != null ? tile.getTileId() : event.plotId();
        List<String> exits = new ArrayList<>();
        if (tile != null) {
            for (com.demo.island.world.Direction8 dir : com.demo.island.world.Direction8.values()) {
                session.getMap().get(tile.getPosition().step(dir)).ifPresent(t -> exits.add(dir.name()));
            }
        }

        List<String> visibleItems = session.getThingIndex().getThingsInPlot(session.getLocation().getTileId()).stream()
                .filter(t -> t.getKind() == com.demo.island.world.ThingKind.ITEM)
                .map(com.demo.island.world.Thing::getName)
                .toList();

        List<String> inventory = session.getThingIndex().getAll().values().stream()
                .filter(t -> t instanceof com.demo.island.world.ItemThing it && "THING_PLAYER".equals(it.getCarriedByCharacterId()))
                .map(com.demo.island.world.Thing::getName)
                .toList();

        GhostState.PlayerView player = new GhostState.PlayerView("Player", plotName, coords, inventory);
        GhostState.PlotView plot = new GhostState.PlotView(event.plotId(), plotName,
                tile != null ? tile.getBiome() : "",
                tile != null ? tile.getRegion() : "",
                tile != null && tile.getContext() != null ? tile.getContext().getCurrentDescription() : "",
                exits,
                visibleItems);

        boolean anchoredHere = GhostPresenceTracker.isGhostAnchor(event.plotId());
        GhostState.GhostView ghost = new GhostState.GhostView(
                anchoredHere,
                new ArrayList<>(GhostPresenceTracker.anchorPlotIds()),
                session.getLastGhostMode(),
                session.getLastGhostText(),
                session.getGhostManifestCount(event.plotId())
        );
        GhostState.PresenceView presence = new GhostState.PresenceView(true, event.reason());
        return new GhostState(turn, time, phase, player, plot, ghost, presence);
    }
}
