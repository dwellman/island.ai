package com.demo.island.engine;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.Player;
import com.demo.island.core.Tile;
import com.demo.island.core.WorldState;
import com.demo.island.engine.actions.ItemActionHandler;
import com.demo.island.engine.actions.ItemActionRegistry;
import com.demo.island.engine.actions.ItemActionResult;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SimpleDmStubAgent implements DmAgent {

    @Override
    public DmDecision decide(DmInput input) {
        WorldState worldState = input.getWorldState();
        Player player = worldState.getPlayer(input.getCommand().getPlayerId());
        if (player == null) {
            return new DmDecision("No such player.", false);
        }

        Tile currentTile = worldState.getTile(player.getCurrentTileId());
        String commandText = input.getCommand().getRawText();
        String upper = commandText.toUpperCase(Locale.ROOT);

        if (upper.equals("LOOK")) {
            return new DmDecision("Look at what?", true);
        }

        if (upper.startsWith("LOOK ")) {
            DmDecision decision = describeTile(worldState, currentTile);
            return new DmDecision(decision.getNarration(), true);
        }

        if (upper.equals("GO")) {
            return new DmDecision("Go where? North, south, toward the path?", true);
        }
        if (upper.equals("TAKE")) {
            return new DmDecision("Take what?", true);
        }
        if (upper.equals("DROP")) {
            return new DmDecision("Drop what?", true);
        }
        if (upper.equals("JUMP")) {
            return new DmDecision("Jump where or over what?", true);
        }
        if (upper.equals("SWIM")) {
            return new DmDecision("Swim where?", true);
        }
        if (upper.equals("CLIMB")) {
            return new DmDecision("Climb what?", true);
        }

        if (upper.startsWith("GO ")) {
            return handleMove(worldState, player, currentTile, upper.substring(3).trim());
        }

        if (upper.startsWith("TAKE ")) {
            return handleTake(worldState, player, currentTile, commandText.substring(5).trim());
        }

        if (upper.startsWith("DROP ")) {
            return handleDrop(worldState, player, currentTile, commandText.substring(5).trim());
        }

        if (upper.startsWith("JUMP ") || upper.startsWith("SWIM ") || upper.startsWith("CLIMB ")) {
            return new DmDecision("You try, but that action isn’t supported yet.", true);
        }

        if (upper.startsWith("MOVE ")) {
            return handleMoveItem(worldState, player, currentTile, commandText.substring(5).trim());
        }

        if (upper.equals("INVENTORY") || upper.equals("INV")) {
            return describeInventory(worldState, player);
        }

        return new DmDecision("You are not sure how to do that yet.", false);
    }

    private DmDecision describeTile(WorldState worldState, Tile tile) {
        String itemsHere = worldState.getItems().values().stream()
                .filter(item -> item.getOwnerKind() == ItemInstance.OwnerKind.TILE)
                .filter(item -> tile.getTileId().equals(item.getOwnerId()))
                .map(item -> "- " + item.getTextFace().getShortDescription())
                .collect(Collectors.joining("\n"));
        String narration = tile.getTextFace().getDetailDescription();
        if (!itemsHere.isEmpty()) {
            narration += "\nItems here:\n" + itemsHere;
        }
        return new DmDecision(narration, false);
    }

    private DmDecision describeInventory(WorldState worldState, Player player) {
        List<String> lines = player.getInventoryItemIds().stream()
                .map(worldState::getItem)
                .filter(item -> item != null)
                .map(item -> "- " + item.getTextFace().getName())
                .collect(Collectors.toList());
        String narration = lines.isEmpty() ? "You are carrying nothing." : "You are carrying:\n" + String.join("\n", lines);
        return new DmDecision(narration, false);
    }

    private DmDecision handleMove(WorldState worldState, Player player, Tile currentTile, String dirToken) {
        Tile.Direction direction;
        try {
            direction = Tile.Direction.valueOf(dirToken.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return new DmDecision("You cannot go that way.", true);
        }
        String neighborId = currentTile.getNeighbor(direction);
        if (neighborId == null) {
            return new DmDecision("There is no path in that direction.", true);
        }
        DmDecision decision = new DmDecision("You move " + dirToken.toUpperCase(Locale.ROOT) + ".", true);
        decision.addStateChange(new MovePlayerChange(player.getPlayerId(), neighborId));
        return decision;
    }

    private DmDecision handleTake(WorldState worldState, Player player, Tile tile, String target) {
        Optional<ItemInstance> itemOpt = findItemOnTile(worldState, tile, target);
        if (itemOpt.isEmpty()) {
            return new DmDecision("There is nothing like that here.", false);
        }
        ItemInstance item = itemOpt.get();
        DmDecision decision = new DmDecision("You pick up the " + item.getTextFace().getName() + ".", true);
        decision.addStateChange(new TransferItemChange(item.getItemId(), ItemInstance.OwnerKind.PLAYER, player.getPlayerId()));
        return decision;
    }

    private DmDecision handleDrop(WorldState worldState, Player player, Tile tile, String target) {
        Optional<ItemInstance> itemOpt = findItemInInventory(worldState, player, target);
        if (itemOpt.isEmpty()) {
            return new DmDecision("You are not holding that.", false);
        }
        ItemInstance item = itemOpt.get();
        DmDecision decision = new DmDecision("You drop the " + item.getTextFace().getName() + ".", true);
        decision.addStateChange(new TransferItemChange(item.getItemId(), ItemInstance.OwnerKind.TILE, tile.getTileId()));
        return decision;
    }

    private DmDecision handleMoveItem(WorldState worldState, Player player, Tile tile, String target) {
        Optional<ItemInstance> itemOpt = findItemOnTile(worldState, tile, target);
        if (itemOpt.isEmpty()) {
            return new DmDecision("You fumble around but fail to move that.", false);
        }
        ItemInstance item = itemOpt.get();
        ItemActionHandler handler = findHandlerForVerb(worldState, item, "MOVE");
        if (handler == null) {
            return new DmDecision("Nothing happens when you move it.", false);
        }

        ItemActionResult result = handler.apply(worldState, player, item);
        DmDecision decision = new DmDecision(result.getNarration(), true);
        result.getStateChanges().forEach(decision::addStateChange);
        return decision;
    }

    private ItemActionHandler findHandlerForVerb(WorldState worldState, ItemInstance item, String verb) {
        return Optional.ofNullable(worldState.getItemType(item.getItemTypeId()))
                .flatMap(type -> type.getActionHooks().stream()
                        .map(ItemActionRegistry::find)
                        .filter(handler -> handler != null && handler.supportsVerb(verb))
                        .findFirst())
                .orElse(null);
    }

    private Optional<ItemInstance> findItemOnTile(WorldState worldState, Tile tile, String target) {
        String normalized = target.toUpperCase(Locale.ROOT);
        return worldState.getItems().values().stream()
                .filter(item -> item.getOwnerKind() == ItemInstance.OwnerKind.TILE)
                .filter(item -> tile.getTileId().equals(item.getOwnerId()))
                .filter(item -> matchesItem(item, normalized))
                .findFirst();
    }

    private Optional<ItemInstance> findItemInInventory(WorldState worldState, Player player, String target) {
        String normalized = target.toUpperCase(Locale.ROOT);
        return player.getInventoryItemIds().stream()
                .map(worldState::getItem)
                .filter(item -> item != null && matchesItem(item, normalized))
                .findFirst();
    }

    private boolean matchesItem(ItemInstance item, String normalizedTarget) {
        String name = item.getTextFace().getName().toUpperCase(Locale.ROOT);
        return name.contains(normalizedTarget) || item.getItemTypeId().toUpperCase(Locale.ROOT).contains(normalizedTarget);
    }
}
