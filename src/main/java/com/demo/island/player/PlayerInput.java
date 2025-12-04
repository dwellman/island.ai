package com.demo.island.player;

import com.demo.island.core.WorldState;
import com.demo.island.dto.ActorEventDto;
import com.demo.island.dto.CheckResultDto;
import com.demo.island.dto.CreatureSnapshot;
import com.demo.island.dto.ItemSnapshot;
import com.demo.island.dto.PlayerSnapshot;
import com.demo.island.dto.SessionSnapshot;
import com.demo.island.dto.TileEventDto;
import com.demo.island.dto.TileSnapshot;
import com.demo.island.engine.check.CheckResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trimmed snapshot for player decision making.
 */
public final class PlayerInput {

    private final WorldState worldState;
    private final String playerId;
    private final List<CheckResult> recentChecks;
    private final String recentNarration;

    public PlayerInput(WorldState worldState, String playerId, List<CheckResult> recentChecks, String recentNarration) {
        this.worldState = worldState;
        this.playerId = playerId;
        this.recentChecks = recentChecks == null ? List.of() : List.copyOf(recentChecks);
        this.recentNarration = recentNarration;
    }

    public PlayerInputDto toDto() {
        SessionSnapshot session = new SessionSnapshot(
                worldState.getSession().getSessionId(),
                worldState.getSession().getTurnNumber(),
                worldState.getSession().getMaxTurns(),
                worldState.getSession().getTimePhase().name(),
                worldState.getSession().isMidnightReached(),
                worldState.getSession().isGhostAwakened(),
                0
        );

        com.demo.island.core.Player player = worldState.getPlayer(playerId);
        PlayerSnapshot playerSnapshot = new PlayerSnapshot(
                player.getPlayerId(),
                player.getName(),
                player.getAvatarType(),
                player.getCurrentTileId(),
                player.getStats().asUnmodifiableMap(),
                List.copyOf(player.getInventoryItemIds()),
                player.getRecentEvents().stream()
                        .limit(5)
                        .map(ev -> new ActorEventDto(
                                ev.getTurnNumber(),
                                ev.getLocationTileId(),
                                ev.getEventType(),
                                ev.getOtherActorId(),
                                ev.getSummary()
                        ))
                        .collect(Collectors.toList())
        );

        com.demo.island.core.Tile currentTile = worldState.getTile(player.getCurrentTileId());
        TileSnapshot tileSnapshot = mapTile(currentTile);

        List<ItemSnapshot> visibleItems = collectVisibleItems(currentTile, player);

        List<CreatureSnapshot> nearbyCreatures = worldState.getCreatures().values().stream()
                .filter(c -> c.getCurrentTileId().equals(player.getCurrentTileId()))
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

        List<CheckResultDto> checkDtos = recentChecks.stream()
                .filter(cr -> cr.getRequest().getSubjectId().equals(playerId))
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

        return new PlayerInputDto(session, playerSnapshot, tileSnapshot, visibleItems, nearbyCreatures, checkDtos, recentNarration);
    }

    private ActorEventDto mapActorEvent(com.demo.island.core.ActorEvent ev) {
        return new ActorEventDto(
                ev.getTurnNumber(),
                ev.getLocationTileId(),
                ev.getEventType(),
                ev.getOtherActorId(),
                ev.getSummary()
        );
    }

    private TileSnapshot mapTile(com.demo.island.core.Tile tile) {
        Map<String, String> neighbors = tile.getNeighbors().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        List<ItemSnapshot> itemsHere = worldState.getItems().values().stream()
                .filter(item -> item.getOwnerKind() == com.demo.island.core.ItemInstance.OwnerKind.TILE)
                .filter(item -> tile.getTileId().equals(item.getOwnerId()))
                .map(this::mapItem)
                .collect(Collectors.toList());
        List<TileEventDto> recentEvents = tile.getRecentEvents().stream()
                .limit(5)
                .map(ev -> new TileEventDto(
                        ev.getTileId(),
                        ev.getTurnNumber(),
                        ev.getActorId(),
                        ev.getEventType(),
                        ev.getSummary()
                ))
                .collect(Collectors.toList());
        return new TileSnapshot(
                tile.getTileId(),
                tile.getBiome(),
                tile.getRegion(),
                tile.getTextFace().getName(),
                tile.getTextFace().getShortDescription(),
                tile.getTextFace().getDetailDescription(),
                tile.getTextFace().getHistory(),
                tile.isDiscovered(),
                neighbors,
                itemsHere,
                recentEvents
        );
    }

    private List<ItemSnapshot> collectVisibleItems(com.demo.island.core.Tile tile, com.demo.island.core.Player player) {
        List<ItemSnapshot> items = new ArrayList<>();
        worldState.getItems().values().forEach(item -> {
            boolean inTile = item.getOwnerKind() == com.demo.island.core.ItemInstance.OwnerKind.TILE
                    && tile.getTileId().equals(item.getOwnerId());
            boolean inInv = item.getOwnerKind() == com.demo.island.core.ItemInstance.OwnerKind.PLAYER
                    && player.getPlayerId().equals(item.getOwnerId());
            if (inTile || inInv) {
                items.add(mapItem(item));
            }
        });
        return items;
    }

    private ItemSnapshot mapItem(com.demo.island.core.ItemInstance instance) {
        var type = worldState.getItemType(instance.getItemTypeId());
        List<String> tags = type != null ? type.getTags() : List.of();
        return new ItemSnapshot(
                instance.getItemId(),
                instance.getItemTypeId(),
                instance.getOwnerKind().name(),
                instance.getOwnerId(),
                instance.getContainedByItemId(),
                instance.getTextFace().getName(),
                instance.getTextFace().getShortDescription(),
                instance.getTextFace().getDetailDescription(),
                instance.getTextFace().getHistory(),
                tags,
                instance.getStats().asUnmodifiableMap()
        );
    }
}
