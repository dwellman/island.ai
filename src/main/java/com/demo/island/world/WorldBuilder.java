package com.demo.island.world;

import com.demo.island.core.Creature;
import com.demo.island.core.GameSession;
import com.demo.island.core.ItemInstance;
import com.demo.island.core.ItemType;
import com.demo.island.core.Player;
import com.demo.island.core.TextFace;
import com.demo.island.core.Tile;
import com.demo.island.core.WorldState;
import com.demo.island.engine.actions.StandardHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Builds a canonical static island layout.
 */
public final class WorldBuilder {

    private static final Logger LOG = LogManager.getLogger(WorldBuilder.class);

    private WorldBuilder() {
    }

    public static WorldState build(String sessionId) {
        GameSession session = new GameSession(sessionId, 12);
        WorldState world = new WorldState(session);

        buildTiles(world);
        buildItemTypes(world);
        buildItems(world);
        buildCreatures(world);
        buildPlayer(world);

        LOG.info("WORLD: start=T_CAMP neighbors={N=T_BAMBOO, E=T_VINES}");
        return world;
    }

    private static void buildTiles(WorldState world) {
        Tile camp = new Tile(
                "T_CAMP",
                "beach",
                "coast",
                new TextFace(
                        "Beach Camp",
                        "A crescent of sand with a smoldering fire pit and scattered driftwood.",
                        "Waves roll under a darkening sky. A ring of driftwood marks the rough outline of a raft-to-be.",
                        "The camp is where you first washed ashore."
                )
        );
        Tile bamboo = new Tile(
                "T_BAMBOO",
                "bamboo_forest",
                "interior",
                new TextFace(
                        "Bamboo Grove",
                        "A dense stand of bamboo whispering in the breeze.",
                        "Tall green stalks crowd together, their leaves ticking softly above your head.",
                        "A castaway's remains lean against one stalk."
                )
        );
        Tile vines = new Tile(
                "T_VINES",
                "vine_forest",
                "interior",
                new TextFace(
                        "Vine Forest",
                        "A tangle of vines draped from low branches.",
                        "Long cords of green sway gently, some thick enough to braid into rope.",
                        "You can hear monkeys somewhere above."
                )
        );

        camp.connect(Tile.Direction.N, bamboo.getTileId());
        camp.connect(Tile.Direction.E, vines.getTileId());
        bamboo.connect(Tile.Direction.S, camp.getTileId());
        vines.connect(Tile.Direction.W, camp.getTileId());

        world.getTiles().put(camp.getTileId(), camp);
        world.getTiles().put(bamboo.getTileId(), bamboo);
        world.getTiles().put(vines.getTileId(), vines);
    }

    private static void buildItemTypes(WorldState world) {
        ItemType skeletonType = new ItemType(
                "skeleton_body",
                new TextFace(
                        "Castaway's Skeleton",
                        "A sun-bleached skeleton slumped against a bamboo stalk.",
                        "Bones sit in a loose huddle at the base of a thick stalk, tattered cloth still clinging to the ribs.",
                        "No one has disturbed it yet."
                ),
                List.of("corpse", "container"),
                List.of(StandardHooks.MOVE_REVEALS_CONTENTS_AWAKENS_GHOST)
        );
        ItemType macheteType = new ItemType(
                "machete",
                new TextFace(
                        "Crude Machete",
                        "A rusted machete with a wrapped handle.",
                        "The blade is nicked and stained, but still more than sharp enough to bite into bamboo.",
                        "It rests with its former owner."
                ),
                List.of("tool", "cutting_tool"),
                List.of()
        );
        ItemType vineBundleType = new ItemType(
                "vine_bundle",
                new TextFace(
                        "Bundle of Vines",
                        "A loosely coiled bundle of fresh vines.",
                        "The vines are supple and smell of earth; they could be braided into rope.",
                        "Cut recently from the forest."
                ),
                List.of("resource", "rope_material"),
                List.of()
        );
        ItemType driftwoodType = new ItemType(
                "driftwood_log",
                new TextFace(
                        "Driftwood Log",
                        "A salt-worn log bleached by the sun.",
                        "Heavy but buoyant, it could form part of a raft.",
                        "Washed up on the beach."
                ),
                List.of("resource", "raft_material"),
                List.of()
        );
        ItemType firePitType = new ItemType(
                "fire_pit",
                new TextFace(
                        "Cold Fire Pit",
                        "A ring of stones with cold ash in the center.",
                        "Someone built a fire here; the embers are long dead.",
                        "Your starting campfire spot."
                ),
                List.of("environment", "camp"),
                List.of()
        );

        world.getItemTypes().put(skeletonType.getItemTypeId(), skeletonType);
        world.getItemTypes().put(macheteType.getItemTypeId(), macheteType);
        world.getItemTypes().put(vineBundleType.getItemTypeId(), vineBundleType);
        world.getItemTypes().put(driftwoodType.getItemTypeId(), driftwoodType);
        world.getItemTypes().put(firePitType.getItemTypeId(), firePitType);
    }

    private static void buildItems(WorldState world) {
        ItemInstance skeleton = new ItemInstance(
                "item-" + UUID.randomUUID(),
                "skeleton_body",
                ItemInstance.OwnerKind.TILE,
                "T_BAMBOO",
                world.getItemType("skeleton_body").getBaseText()
        );

        ItemInstance machete = new ItemInstance(
                "item-" + UUID.randomUUID(),
                "machete",
                ItemInstance.OwnerKind.ITEM,
                skeleton.getItemId(),
                world.getItemType("machete").getBaseText()
        );
        machete.setContainedByItemId(skeleton.getItemId());

        ItemInstance vinesBundle = new ItemInstance(
                "item-" + UUID.randomUUID(),
                "vine_bundle",
                ItemInstance.OwnerKind.TILE,
                "T_VINES",
                world.getItemType("vine_bundle").getBaseText()
        );

        ItemInstance driftwood = new ItemInstance(
                "item-" + UUID.randomUUID(),
                "driftwood_log",
                ItemInstance.OwnerKind.TILE,
                "T_CAMP",
                world.getItemType("driftwood_log").getBaseText()
        );

        ItemInstance firePit = new ItemInstance(
                "item-" + UUID.randomUUID(),
                "fire_pit",
                ItemInstance.OwnerKind.TILE,
                "T_CAMP",
                world.getItemType("fire_pit").getBaseText()
        );

        world.getItems().put(skeleton.getItemId(), skeleton);
        world.getItems().put(machete.getItemId(), machete);
        world.getItems().put(vinesBundle.getItemId(), vinesBundle);
        world.getItems().put(driftwood.getItemId(), driftwood);
        world.getItems().put(firePit.getItemId(), firePit);
    }

    private static void buildCreatures(WorldState world) {
        Creature ghost = new Creature(
                "ghost",
                Creature.CreatureKind.GHOST,
                "T_CAMP",
                new TextFace(
                        "Smoke Walker",
                        "A thin silhouette of drifting smoke.",
                        "It coils and shivers at the edge of vision, silent until stirred.",
                        "Bound to the island's unrest."
                )
        );

        Creature monkeys = new Creature(
                "monkeys",
                Creature.CreatureKind.MONKEY_TROOP,
                "T_VINES",
                new TextFace(
                        "Vine Monkeys",
                        "A troop of curious monkeys perched in the vines.",
                        "They chatter and watch, eager for fruit and mischief.",
                        "Their home is the vine forest."
                )
        );

        world.getCreatures().put(ghost.getCreatureId(), ghost);
        world.getCreatures().put(monkeys.getCreatureId(), monkeys);
    }

    private static void buildPlayer(WorldState world) {
        Player player = new Player(
                "player-1",
                "Rae",
                "Navigator",
                "T_CAMP",
                new TextFace(
                        "Rae the Navigator",
                        "A cautious navigator, eyes always scanning the horizon.",
                        "Rae smells of salt and smoke, clothes still mostly dry but shoes full of sand.",
                        "Rae has just arrived on the island."
                )
        );
        player.getStats().set("STR", 12);
        player.getStats().set("AGI", 14);
        player.getStats().set("CHA", 10);
        player.getStats().set("AWR", 13);

        world.getPlayers().put(player.getPlayerId(), player);
    }
}
