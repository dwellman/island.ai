package com.demo.island;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.WorldState;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.TurnEngine;
import com.demo.island.engine.check.CheckService;
import com.demo.island.engine.dice.DiceService;
import com.demo.island.monkey.MonkeyAgent;
import com.demo.island.monkey.MonkeyDecision;
import com.demo.island.monkey.MonkeyInput;
import com.demo.island.monkey.MonkeyIntent;
import com.demo.island.store.GameRepository;
import com.demo.island.store.InMemoryGameRepository;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies mapping of monkey intents to engine effects (banana steal, poo flag, return home).
 */
public class MonkeyIntentEffectsTest {

    private WorldState worldState;
    private TurnEngine turnEngine;
    private TestMonkeyAgent monkeyAgent;
    private GameRepository gameRepository;

    @BeforeEach
    void setup() {
        worldState = WorldFactory.createDemoWorld("monkey-intents");
        gameRepository = new InMemoryGameRepository();
        gameRepository.createNewSession(worldState);

        monkeyAgent = new TestMonkeyAgent();
        DiceService diceService = new DiceService();
        CheckService checkService = new CheckService(diceService);
        turnEngine = new TurnEngine(diceService, checkService, input -> null, monkeyAgent);
    }

    @Test
    void stealBananaTransfersOwnership() {
        // give player a banana (tag bait)
        ItemInstance banana = new ItemInstance("banana-1", "banana", ItemInstance.OwnerKind.PLAYER, "player-1",
                new com.demo.island.core.TextFace("Banana", "A ripe banana", "Sweet", ""));
        worldState.getItemTypes().put("banana", new com.demo.island.core.ItemType("banana",
                new com.demo.island.core.TextFace("Banana", "A ripe banana", "Sweet", ""),
                java.util.List.of("bait"), java.util.List.of()));
        worldState.getItems().put(banana.getItemId(), banana);
        worldState.getPlayer("player-1").addItem(banana.getItemId());

        // place the existing monkey troop in same tile as player (camp)
        com.demo.island.core.Creature monkey = worldState.getCreature("monkeys");
        monkey.moveToTile("T_CAMP");

        monkeyAgent.nextDecision = new MonkeyDecision("steal banana", true);
        monkeyAgent.nextDecision.addIntent(MonkeyIntent.targeting(MonkeyIntent.IntentKind.STEAL_BANANA_FROM_PLAYER, "player-1"));

        DmAgent consumingDm = input -> new DmDecision("tick", true);
        turnEngine.runTurn(worldState, new PlayerCommand("player-1", "LOOK"), consumingDm);

        ItemInstance updatedBanana = worldState.getItem(banana.getItemId());
        assertThat(updatedBanana.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.CREATURE);
        assertThat(updatedBanana.getOwnerId()).isEqualTo(monkey.getCreatureId());
    }

    @Test
    void throwPooSetsPlayerFlag() {
        com.demo.island.core.Creature monkey = worldState.getCreature("monkeys");
        monkey.moveToTile("T_CAMP");

        monkeyAgent.nextDecision = new MonkeyDecision("throw poo", true);
        monkeyAgent.nextDecision.addIntent(MonkeyIntent.targeting(MonkeyIntent.IntentKind.THROW_POO_AT_PLAYER, "player-1"));

        DmAgent consumingDm = input -> new DmDecision("tick", true);
        turnEngine.runTurn(worldState, new PlayerCommand("player-1", "LOOK"), consumingDm);

        assertThat(worldState.getPlayer("player-1").getStats().has("POOED")).isTrue();
    }

    @Test
    void returnHomeSetsTargetAndSleepFlag() {
        com.demo.island.core.Creature monkey = worldState.getCreature("monkeys");
        monkey.moveToTile("T_BAMBOO");

        monkeyAgent.nextDecision = new MonkeyDecision("sleep", true);
        monkeyAgent.nextDecision.addIntent(MonkeyIntent.of(MonkeyIntent.IntentKind.RETURN_HOME_AND_SLEEP));

        DmAgent consumingDm = input -> new DmDecision("tick", true);
        turnEngine.runTurn(worldState, new PlayerCommand("player-1", "LOOK"), consumingDm);

        com.demo.island.core.Creature updated = worldState.getCreature(monkey.getCreatureId());
        assertThat(updated.getTargetTileId()).isEqualTo("T_VINES");
    }

    private static class TestMonkeyAgent implements MonkeyAgent {
        MonkeyDecision nextDecision;

        @Override
        public MonkeyDecision decide(MonkeyInput input) {
            return nextDecision;
        }
    }
}
