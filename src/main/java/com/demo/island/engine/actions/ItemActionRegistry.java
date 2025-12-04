package com.demo.island.engine.actions;

import java.util.HashMap;
import java.util.Map;

public final class ItemActionRegistry {

    private static final Map<String, ItemActionHandler> REGISTRY = new HashMap<>();

    static {
        register(new SkeletonMoveHook());
    }

    private ItemActionRegistry() {
    }

    public static void register(ItemActionHandler handler) {
        REGISTRY.put(handler.getHookId(), handler);
    }

    public static ItemActionHandler find(String hookId) {
        return REGISTRY.get(hookId);
    }
}
