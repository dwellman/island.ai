package com.demo.island.world;

import com.demo.island.game.Ability;
import com.demo.island.game.GameItemType;
import com.demo.island.game.Mood;
import com.demo.island.game.Skill;

import java.util.Set;

/**
 * Seeds Things into the world and returns an index.
 */
public final class WorldThingSeeder {

    private WorldThingSeeder() {
    }

    public static WorldThingIndex seed(IslandMap map) {
        WorldThingIndex index = new WorldThingIndex(map);

        CharacterThing player = new CharacterThing("THING_PLAYER", "you", ThingKind.CHARACTER, "T_WRECK_BEACH");
        player.getAbilityScores().put(Ability.STR, 12);
        player.getAbilityScores().put(Ability.DEX, 14);
        player.getAbilityScores().put(Ability.CON, 12);
        player.getAbilityScores().put(Ability.INT, 10);
        player.getAbilityScores().put(Ability.WIS, 13);
        player.getAbilityScores().put(Ability.CHA, 10);
        player.computeModsFromScores();
        player.setProficiencyBonus(2);
        player.setHp(10);
        player.setMaxHp(10);
        player.setMood(Mood.CALM);
        player.getTags().add("PLAYER");
        index.registerThing(player);

        CharacterThing ghost = new CharacterThing("THING_GHOST", "ghost", ThingKind.SUPERNATURAL, "T_OLD_RUINS");
        ghost.getAbilityScores().put(Ability.STR, 8);
        ghost.getAbilityScores().put(Ability.DEX, 12);
        ghost.getAbilityScores().put(Ability.CON, 8);
        ghost.getAbilityScores().put(Ability.INT, 12);
        ghost.getAbilityScores().put(Ability.WIS, 16);
        ghost.getAbilityScores().put(Ability.CHA, 14);
        ghost.computeModsFromScores();
        ghost.setProficiencyBonus(2);
        ghost.setHp(12);
        ghost.setMaxHp(12);
        ghost.setMood(Mood.HAUNTING);
        ghost.getBehaviorTags().add("SUPERNATURAL");
        ghost.getTags().addAll(Set.of("GHOST", "SUPERNATURAL"));
        ghost.getHomePlotIds().add("T_OLD_RUINS");
        ghost.getHomePlotIds().add("T_CAVE_ENTRANCE");
        index.registerThing(ghost);

        CharacterThing monkeys = new CharacterThing("THING_MONKEY_TROOP_CAMP", "troop of monkeys", ThingKind.CHARACTER, "T_VINE_FOREST");
        monkeys.getAbilityScores().put(Ability.STR, 10);
        monkeys.getAbilityScores().put(Ability.DEX, 15);
        monkeys.getAbilityScores().put(Ability.CON, 12);
        monkeys.getAbilityScores().put(Ability.INT, 8);
        monkeys.getAbilityScores().put(Ability.WIS, 12);
        monkeys.getAbilityScores().put(Ability.CHA, 9);
        monkeys.computeModsFromScores();
        monkeys.setProficiencyBonus(2);
        monkeys.setHp(8);
        monkeys.setMaxHp(8);
        monkeys.setMood(Mood.CURIOUS);
        monkeys.getSkillProficiencies().add(Skill.PERCEPTION);
        monkeys.getSkillProficiencies().add(Skill.ACROBATICS);
        monkeys.getBehaviorTags().add("MONKEY_TROOP");
        monkeys.getTags().add("MONKEY_TROOP");
        monkeys.getHomePlotIds().add("T_VINE_FOREST");
        index.registerThing(monkeys);

        ItemThing hatchet = new ItemThing("THING_HATCHET", "rusty hatchet", GameItemType.HATCHET, "T_WRECK_BEACH");
        hatchet.getTags().add("TOOL");
        index.registerThing(hatchet);

        return index;
    }
}
