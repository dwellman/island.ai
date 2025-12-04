package com.demo.island.tools;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.WorldState;
import com.demo.island.engine.check.CheckRequest;
import com.demo.island.engine.check.CheckSubjectKind;
import com.demo.island.engine.check.CheckType;
import com.demo.island.monkey.MonkeyIntent;

import java.util.List;
import java.util.Optional;

/**
 * Helpers for monkey decision building: banana carriers, moods, intents, and checks.
 */
public final class MonkeyTools {

    public Optional<String> findBananaCarrier(WorldState worldState) {
        return worldState.getPlayers().values().stream()
                .filter(player -> player.getInventoryItemIds().stream()
                        .map(worldState::getItem)
                        .anyMatch(item -> item != null && hasTag(worldState, item, "bait")))
                .map(com.demo.island.core.Player::getPlayerId)
                .findFirst();
    }

    public MonkeyIntent followBanana(String playerId) {
        return MonkeyIntent.targeting(MonkeyIntent.IntentKind.FOLLOW_BANANA_CARRIER, playerId);
    }

    public MonkeyIntent stealBanana(String playerId) {
        return MonkeyIntent.targeting(MonkeyIntent.IntentKind.STEAL_BANANA_FROM_PLAYER, playerId);
    }

    public MonkeyIntent throwPoo(String playerId) {
        return MonkeyIntent.targeting(MonkeyIntent.IntentKind.THROW_POO_AT_PLAYER, playerId);
    }

    public MonkeyIntent returnHome() {
        return MonkeyIntent.of(MonkeyIntent.IntentKind.RETURN_HOME_AND_SLEEP);
    }

    public MonkeyIntent ignore() {
        return MonkeyIntent.of(MonkeyIntent.IntentKind.IGNORE_PLAYERS);
    }

    public CheckRequest genericCheck(String subjectId, int dc) {
        return new CheckRequest(CheckType.GENERIC, CheckSubjectKind.CREATURE, subjectId, dc);
    }

    public String moodFromIntimidation(List<com.demo.island.engine.check.CheckResult> checks, String monkeyId) {
        return checks.stream()
                .filter(cr -> cr.getRequest().getType() == CheckType.INTIMIDATION)
                .filter(cr -> monkeyId.equals(cr.getRequest().getSubjectId()))
                .findFirst()
                .map(cr -> cr.isSuccess() ? "CALM" : "AGITATED")
                .orElse(null);
    }

    private boolean hasTag(WorldState worldState, ItemInstance item, String tag) {
        return Optional.ofNullable(worldState.getItemType(item.getItemTypeId()))
                .map(type -> type.getTags().contains(tag))
                .orElse(false);
    }
}
