package com.demo.island.world;

import com.demo.island.game.Ability;
import com.demo.island.game.Mood;
import com.demo.island.game.Skill;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CharacterThing extends Thing {

    private final Map<Ability, Integer> abilityScores = new EnumMap<>(Ability.class);
    private final Map<Ability, Integer> abilityMods = new EnumMap<>(Ability.class);
    private int proficiencyBonus;
    private final Set<Skill> skillProficiencies = new HashSet<>();
    private final Set<Ability> saveProficiencies = new HashSet<>();
    private int hp;
    private int maxHp;
    private Mood mood;
    private final Set<String> behaviorTags = new HashSet<>();

    public CharacterThing(String id, String name, ThingKind kind, String currentPlotId) {
        super(id, name, kind, currentPlotId);
    }

    public Map<Ability, Integer> getAbilityScores() {
        return abilityScores;
    }

    public Map<Ability, Integer> getAbilityMods() {
        return abilityMods;
    }

    public int getProficiencyBonus() {
        return proficiencyBonus;
    }

    public void setProficiencyBonus(int proficiencyBonus) {
        this.proficiencyBonus = proficiencyBonus;
    }

    public Set<Skill> getSkillProficiencies() {
        return skillProficiencies;
    }

    public Set<Ability> getSaveProficiencies() {
        return saveProficiencies;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public Set<String> getBehaviorTags() {
        return behaviorTags;
    }

    public void computeModsFromScores() {
        for (Map.Entry<Ability, Integer> entry : abilityScores.entrySet()) {
            int score = entry.getValue();
            int mod = (int) Math.floor((score - 10) / 2.0);
            abilityMods.put(entry.getKey(), mod);
        }
    }
}
