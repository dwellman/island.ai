package com.demo.island.engine;

import com.demo.island.core.WorldState;
import com.demo.island.dto.DmInputDto;

public final class DmInput {

    private final WorldState worldState;
    private final PlayerCommand command;
    private final java.util.List<com.demo.island.engine.check.CheckResult> recentCheckResults;

    public DmInput(WorldState worldState, PlayerCommand command) {
        this(worldState, command, java.util.List.of());
    }

    public DmInput(WorldState worldState, PlayerCommand command, java.util.List<com.demo.island.engine.check.CheckResult> recentCheckResults) {
        this.worldState = worldState;
        this.command = command;
        this.recentCheckResults = recentCheckResults;
    }

    public WorldState getWorldState() {
        return worldState;
    }

    public PlayerCommand getCommand() {
        return command;
    }

    public DmInputDto toDto() {
        DmInputDto base = DmInputDto.from(worldState, command);
        java.util.List<com.demo.island.dto.CheckResultDto> dtos = recentCheckResults.stream()
                .map(cr -> new com.demo.island.dto.CheckResultDto(
                        cr.getCheckId(),
                        cr.getRequest().getType().name(),
                        cr.getRequest().getSubjectKind().name(),
                        cr.getRequest().getSubjectId(),
                        cr.getRequest().getDifficulty(),
                        cr.getRoll(),
                        cr.getModifier(),
                        cr.getTotal(),
                        cr.isSuccess()
                ))
                .toList();
        return com.demo.island.dto.DmInputDto.withChecks(base, dtos);
    }
}
