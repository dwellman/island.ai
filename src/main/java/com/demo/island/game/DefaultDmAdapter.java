package com.demo.island.game;

import com.demo.island.dto.PlotContext;

public final class DefaultDmAdapter implements DmAdapter {

    @Override
    public String narrate(TurnContext context) {
        // Monkey poo overrides if present.
        String monkey = monkeyNarration(context);
        String base = baseNarration(context);
        if (monkey.isEmpty()) {
            return base;
        }
        if (base.isEmpty()) {
            return monkey;
        }
        return base + " " + monkey;
    }

    private String baseNarration(TurnContext ctx) {
        if (ctx.lastActionType == null) {
            return "You are standing in the dark just before dawn. You have no idea how you got here.";
        }
        return switch (ctx.lastActionType) {
            case LOOK -> lookNarration(ctx.plotContext);
            case MOVE_WALK, MOVE_RUN -> moveNarration(ctx);
            case JUMP -> jumpNarration(ctx);
            case SEARCH -> "You search the area.";
            case PICK_UP, DROP, RAFT_WORK_SMALL, RAFT_WORK_MAJOR, LAUNCH_RAFT -> ctx.lastActionResultSummary;
        };
    }

    private String lookNarration(PlotContext plot) {
        return plot != null && plot.currentDescription != null
                ? plot.currentDescription
                : "You take a quick look around.";
    }

    private String moveNarration(TurnContext ctx) {
        if (!ctx.lastActionSuccess) {
            return "You can't go that way.";
        }
        PlotContext plot = ctx.plotContext;
        String biome = plot != null && plot.biome != null ? plot.biome.replace('_', ' ') : "new ground";
        return "You make your way into a patch of " + biome + ".";
    }

    private String jumpNarration(TurnContext ctx) {
        if (ctx.lastChallengeResult != null && "JUMP_GENERIC".equals(ctx.lastChallengeResult.getChallengeId())) {
            return ctx.lastChallengeResult.isSuccess()
                    ? "You gather yourself and clear the gap, landing on the far side."
                    : "You push off, but come up short and scramble back to where you started.";
        }
        return ctx.lastActionResultSummary;
    }

    private String monkeyNarration(TurnContext ctx) {
        if (ctx.lastChallengeResult == null || ctx.lastChallenge == null) {
            return "";
        }
        if (!"DODGE_MONKEY_POO".equals(ctx.lastChallenge.getChallengeId())) {
            return "";
        }
        return ctx.lastChallengeResult.isSuccess()
                ? "Something wet and foul whistles past your ear and splats into the ground behind you."
                : "A wet smack hits your shoulder. The monkeys above chatter with obvious delight.";
    }
}
