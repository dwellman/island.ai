package com.demo.island.engine.actions;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.Player;
import com.demo.island.core.WorldState;

public interface ItemActionHandler {

    String getHookId();

    boolean supportsVerb(String verb);

    ItemActionResult apply(WorldState worldState, Player player, ItemInstance item);
}
