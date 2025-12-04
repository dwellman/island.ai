package com.demo.island.ghost;

import com.demo.island.core.WorldState;
import com.demo.island.dto.CheckResultDto;
import com.demo.island.dto.CreatureSnapshot;
import com.demo.island.dto.PlayerSnapshot;
import com.demo.island.dto.SessionSnapshot;
import com.demo.island.engine.check.CheckResult;

import java.util.List;
import java.util.stream.Collectors;

public final class GhostInput {

    private final WorldState worldState;
    private final List<CheckResult> recentChecks;

    public GhostInput(WorldState worldState, List<CheckResult> recentChecks) {
        this.worldState = worldState;
        this.recentChecks = recentChecks == null ? List.of() : List.copyOf(recentChecks);
    }

    public WorldState getWorldState() {
        return worldState;
    }

    public List<CheckResult> getRecentChecks() {
        return recentChecks;
    }

    public GhostInputDto toDto() {
        SessionSnapshot sessionSnapshot = new SessionSnapshot(
                worldState.getSession().getSessionId(),
                worldState.getSession().getTurnNumber(),
                worldState.getSession().getMaxTurns(),
                worldState.getSession().getTimePhase().name(),
                worldState.getSession().isMidnightReached(),
                worldState.getSession().isGhostAwakened(),
                0
        );

        List<CreatureSnapshot> ghosts = worldState.getCreatures().values().stream()
                .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.GHOST)
                .map(c -> new CreatureSnapshot(
                        c.getCreatureId(),
                        c.getKind().name(),
                        c.getCurrentTileId(),
                        c.getStats().asUnmodifiableMap(),
                        List.copyOf(c.getCarriedItemIds()),
                        c.getTargetTileId(),
                        c.getRecentEvents().stream()
                                .limit(5)
                                .map(this::mapActorEvent)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        List<PlayerSnapshot> players = worldState.getPlayers().values().stream()
                .map(p -> new PlayerSnapshot(
                        p.getPlayerId(),
                        p.getName(),
                        p.getAvatarType(),
                        p.getCurrentTileId(),
                        p.getStats().asUnmodifiableMap(),
                        List.copyOf(p.getInventoryItemIds()),
                        p.getRecentEvents().stream()
                                .limit(5)
                                .map(this::mapActorEvent)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        List<CheckResultDto> checkDtos = recentChecks.stream()
                .map(cr -> new CheckResultDto(
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
                .collect(Collectors.toList());

        return new GhostInputDto(sessionSnapshot, ghosts, players, checkDtos);
    }

    private com.demo.island.dto.ActorEventDto mapActorEvent(com.demo.island.core.ActorEvent ev) {
        return new com.demo.island.dto.ActorEventDto(
                ev.getTurnNumber(),
                ev.getLocationTileId(),
                ev.getEventType(),
                ev.getOtherActorId(),
                ev.getSummary()
        );
    }
}
