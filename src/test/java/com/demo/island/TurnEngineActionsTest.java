package com.demo.island;

import com.demo.island.core.GameSession;
import com.demo.island.core.ItemInstance;
import com.demo.island.core.Player;
import com.demo.island.core.WorldState;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.TurnEngine;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TurnEngineActionsTest {

    private WorldState worldState;
    private TurnEngine turnEngine;
    private SimpleDmStubAgent dmAgent;
    private final String playerId = "player-1";

    @BeforeEach
    void setUp() {
        worldState = WorldFactory.createDemoWorld("test-session");
        turnEngine = new TurnEngine();
        dmAgent = new SimpleDmStubAgent();
    }

    @Test
    void goCommandMovesPlayerAndUpdatesPhase() {
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "GO N"), dmAgent);

        Player player = worldState.getPlayer(playerId);
        assertThat(player.getCurrentTileId()).isEqualTo("T_BAMBOO");
        assertThat(worldState.getTile("T_BAMBOO").isDiscovered()).isTrue();
        assertThat(worldState.getSession().getTurnNumber()).isEqualTo(1);
        assertThat(worldState.getSession().getTimePhase()).isEqualTo(GameSession.TimePhase.LIGHT);
    }

    @Test
    void movingSkeletonRevealsMacheteAndAwakensGhost() {
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "GO N"), dmAgent);
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "MOVE SKELETON"), dmAgent);

        ItemInstance machete = findByType("machete");
        assertThat(machete.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.TILE);
        assertThat(machete.getOwnerId()).isEqualTo("T_BAMBOO");
        assertThat(machete.getContainedByItemId()).isNull();
        assertThat(worldState.getSession().isGhostAwakened()).isTrue();
    }

    @Test
    void takeAndDropTransfersOwnership() {
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "GO N"), dmAgent);
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "MOVE SKELETON"), dmAgent);
        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "TAKE MACHETE"), dmAgent);

        ItemInstance machete = findByType("machete");
        assertThat(machete.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.PLAYER);
        assertThat(machete.getOwnerId()).isEqualTo(playerId);
        assertThat(worldState.getPlayer(playerId).getInventoryItemIds()).contains(machete.getItemId());

        turnEngine.runTurn(worldState, new PlayerCommand(playerId, "DROP MACHETE"), dmAgent);

        machete = findByType("machete");
        assertThat(machete.getOwnerKind()).isEqualTo(ItemInstance.OwnerKind.TILE);
        assertThat(machete.getOwnerId()).isEqualTo("T_BAMBOO");
        assertThat(worldState.getPlayer(playerId).getInventoryItemIds()).doesNotContain(machete.getItemId());
    }

    private ItemInstance findByType(String typeId) {
        return worldState.getItems().values().stream()
                .filter(item -> item.getItemTypeId().equals(typeId))
                .findFirst()
                .orElseThrow();
    }
}
