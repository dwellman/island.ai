package com.demo.island.game;

import com.demo.island.dto.PlotContext;
import com.demo.island.world.CharacterThing;
import com.demo.island.world.ItemThing;
import com.demo.island.world.WorldThingIndex;

import java.util.List;

public final class ToolContext {
    public final GameSession session;
    public final PlotContext plotContext;
    public final PlayerTool tool;
    public final GameAction action;
    public final String targetRaw;
    public final String reason;
    public final String mood;
    public final String note;
    public final WorldThingIndex thingIndex;
    public final CharacterThing actorThing;
    public final List<ItemThing> visibleItems;
    public final String actorId;

    public ToolContext(GameSession session,
                       PlotContext plotContext,
                       PlayerTool tool,
                       GameAction action,
                       String targetRaw,
                       String reason,
                       String mood,
                       String note,
                       WorldThingIndex thingIndex,
                       CharacterThing actorThing,
                       List<ItemThing> visibleItems,
                       String actorId) {
        this.session = session;
        this.plotContext = plotContext;
        this.tool = tool;
        this.action = action;
        this.targetRaw = targetRaw;
        this.reason = reason;
        this.mood = mood;
        this.note = note;
        this.thingIndex = thingIndex;
        this.actorThing = actorThing;
        this.visibleItems = visibleItems;
        this.actorId = actorId;
    }
}
