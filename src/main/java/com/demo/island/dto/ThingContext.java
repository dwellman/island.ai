package com.demo.island.dto;

import com.demo.island.world.ThingKind;

import java.util.ArrayList;
import java.util.List;

public final class ThingContext {
    private String id;
    private String name;
    private ThingKind kind;
    private String locationPlotId;
    private boolean visibleToPlayer;
    private String shortDescription;
    private String behaviorHint;
    private String statsSummary;
    private List<String> goals = new ArrayList<>();
    private List<String> triggers = new ArrayList<>();
    private List<String> secrets = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ThingKind getKind() {
        return kind;
    }

    public void setKind(ThingKind kind) {
        this.kind = kind;
    }

    public String getLocationPlotId() {
        return locationPlotId;
    }

    public void setLocationPlotId(String locationPlotId) {
        this.locationPlotId = locationPlotId;
    }

    public boolean isVisibleToPlayer() {
        return visibleToPlayer;
    }

    public void setVisibleToPlayer(boolean visibleToPlayer) {
        this.visibleToPlayer = visibleToPlayer;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBehaviorHint() {
        return behaviorHint;
    }

    public void setBehaviorHint(String behaviorHint) {
        this.behaviorHint = behaviorHint;
    }

    public String getStatsSummary() {
        return statsSummary;
    }

    public void setStatsSummary(String statsSummary) {
        this.statsSummary = statsSummary;
    }

    public List<String> getGoals() {
        return goals;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers;
    }

    public List<String> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<String> secrets) {
        this.secrets = secrets;
    }
}
