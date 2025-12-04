package com.demo.island.dto;

import com.demo.island.core.GameSession;
import com.demo.island.core.ItemInstance;
import com.demo.island.core.ItemType;
import com.demo.island.core.Player;
import com.demo.island.core.TextFace;
import com.demo.island.core.Tile;
import com.demo.island.core.TileEvent;
import com.demo.island.core.WorldState;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.dto.ActorEventDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DmInputDto {

    private final SessionSnapshot session;
    private final PlayerCommand command;
    private final List<PlayerSnapshot> players;
    private final List<TileSnapshot> tiles;
    private final List<CreatureSnapshot> creatures;
    private final List<ItemSnapshot> items;
    private final List<CheckResultDto> checkResults;

    public DmInputDto(SessionSnapshot session, PlayerCommand command, List<PlayerSnapshot> players,
                      List<TileSnapshot> tiles, List<CreatureSnapshot> creatures, List<ItemSnapshot> items,
                      List<CheckResultDto> checkResults) {
        this.session = session;
        this.command = command;
        this.players = players;
        this.tiles = tiles;
        this.creatures = creatures;
        this.items = items;
        this.checkResults = checkResults;
    }

    public SessionSnapshot getSession() {
        return session;
    }

    public PlayerCommand getCommand() {
        return command;
    }

    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    public List<TileSnapshot> getTiles() {
        return tiles;
    }

    public List<CreatureSnapshot> getCreatures() {
        return creatures;
    }

    public List<ItemSnapshot> getItems() {
        return items;
    }

    public List<CheckResultDto> getCheckResults() {
        return checkResults;
    }

    public static DmInputDto from(WorldState worldState, PlayerCommand command) {
        GameSession session = worldState.getSession();
        SessionSnapshot sessionSnapshot = new SessionSnapshot(
                session.getSessionId(),
                session.getTurnNumber(),
                session.getMaxTurns(),
                session.getTimePhase().name(),
                session.isMidnightReached(),
                session.isGhostAwakened(),
                0
        );

        Map<String, ItemSnapshot> itemSnapshots = new HashMap<>();
        for (ItemInstance instance : worldState.getItems().values()) {
            ItemType type = worldState.getItemType(instance.getItemTypeId());
            List<String> tags = type != null ? type.getTags() : List.of();
            ItemSnapshot snapshot = new ItemSnapshot(
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
            itemSnapshots.put(instance.getItemId(), snapshot);
        }

        List<TileSnapshot> tileSnapshots = worldState.getTiles().values().stream()
                .map(tile -> mapTile(tile, itemSnapshots, worldState))
                .collect(Collectors.toList());

        List<PlayerSnapshot> playerSnapshots = worldState.getPlayers().values().stream()
                .map(player -> new PlayerSnapshot(
                        player.getPlayerId(),
                        player.getName(),
                        player.getAvatarType(),
                        player.getCurrentTileId(),
                        player.getStats().asUnmodifiableMap(),
                        List.copyOf(player.getInventoryItemIds()),
                        player.getRecentEvents().stream()
                                .limit(5)
                                .map(DmInputDto::mapActorEvent)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        List<CreatureSnapshot> creatureSnapshots = worldState.getCreatures().values().stream()
                .map(creature -> new CreatureSnapshot(
                        creature.getCreatureId(),
                        creature.getKind().name(),
                        creature.getCurrentTileId(),
                        creature.getStats().asUnmodifiableMap(),
                        List.copyOf(creature.getCarriedItemIds()),
                        creature.getTargetTileId(),
                        creature.getRecentEvents().stream()
                                .limit(5)
                                .map(DmInputDto::mapActorEvent)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new DmInputDto(sessionSnapshot, command, playerSnapshots, tileSnapshots, creatureSnapshots,
                new ArrayList<>(itemSnapshots.values()), List.of());
    }

    public static DmInputDto withChecks(DmInputDto base, List<CheckResultDto> checkResults) {
        return new DmInputDto(
                base.session,
                base.command,
                base.players,
                base.tiles,
                base.creatures,
                base.items,
                checkResults
        );
    }

    private static TileSnapshot mapTile(Tile tile, Map<String, ItemSnapshot> itemSnapshots, WorldState worldState) {
        Map<String, String> neighbors = tile.getNeighbors().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        List<ItemSnapshot> presentItems = worldState.getItems().values().stream()
                .filter(item -> item.getOwnerKind() == ItemInstance.OwnerKind.TILE)
                .filter(item -> tile.getTileId().equals(item.getOwnerId()))
                .map(item -> itemSnapshots.get(item.getItemId()))
                .collect(Collectors.toList());
        List<TileEventDto> recentEvents = tile.getRecentEvents().stream()
                .limit(5)
                .map(DmInputDto::mapEvent)
                .collect(Collectors.toList());

        TextFace text = tile.getTextFace();
        return new TileSnapshot(
                tile.getTileId(),
                tile.getBiome(),
                tile.getRegion(),
                text.getName(),
                text.getShortDescription(),
                text.getDetailDescription(),
                text.getHistory(),
                tile.isDiscovered(),
                neighbors,
                presentItems,
                recentEvents
        );
    }

    private static TileEventDto mapEvent(TileEvent event) {
        return new TileEventDto(
                event.getTileId(),
                event.getTurnNumber(),
                event.getActorId(),
                event.getEventType(),
                event.getSummary()
        );
    }

    private static ActorEventDto mapActorEvent(com.demo.island.core.ActorEvent event) {
        return new ActorEventDto(
                event.getTurnNumber(),
                event.getLocationTileId(),
                event.getEventType(),
                event.getOtherActorId(),
                event.getSummary()
        );
    }
}
