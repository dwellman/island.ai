package com.demo.island.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class WorldState {

    private final GameSession session;
    private final Map<String, Tile> tiles = new HashMap<>();
    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, Creature> creatures = new HashMap<>();
    private final Map<String, ItemType> itemTypes = new HashMap<>();
    private final Map<String, ItemInstance> items = new HashMap<>();

    public WorldState(GameSession session) {
        this.session = Objects.requireNonNull(session);
    }

    public GameSession getSession() {
        return session;
    }

    public Map<String, Tile> getTiles() {
        return tiles;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public Map<String, Creature> getCreatures() {
        return creatures;
    }

    public Map<String, ItemType> getItemTypes() {
        return itemTypes;
    }

    public Map<String, ItemInstance> getItems() {
        return items;
    }

    public Tile getTile(String tileId) {
        return tiles.get(tileId);
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public Creature getCreature(String creatureId) {
        return creatures.get(creatureId);
    }

    public ItemInstance getItem(String itemId) {
        return items.get(itemId);
    }

    public ItemType getItemType(String itemTypeId) {
        return itemTypes.get(itemTypeId);
    }
}
