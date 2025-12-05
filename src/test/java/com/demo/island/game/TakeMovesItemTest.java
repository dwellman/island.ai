package com.demo.island.game;

import com.demo.island.world.ItemThing;
import com.demo.island.world.WorldThingIndex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TakeMovesItemTest {

    @Test
    void takeHatchetMovesFromPlotToInventory() {
        GameSession session = GameSession.newSession();
        WorldThingIndex index = session.getThingIndex();

        ItemThing hatchet = (ItemThing) index.getThing("THING_HATCHET");
        assertThat(hatchet).isNotNull();
        assertThat(hatchet.getCurrentPlotId()).isEqualTo("T_WRECK_BEACH");
        assertThat(hatchet.getCarriedByCharacterId()).isNull();
        assertThat(index.getThingsInPlot("T_WRECK_BEACH")).anyMatch(t -> t.getId().equals("THING_HATCHET"));
        assertThat(session.getInventory().getOrDefault(GameItemType.HATCHET, 0)).isZero();

        GameActionResult res = GameEngine.perform(session, GameAction.withItem(GameActionType.PICK_UP, GameItemType.HATCHET));

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getMessage().toLowerCase()).contains("pick up").contains("hatchet");
        assertThat(session.getInventory().getOrDefault(GameItemType.HATCHET, 0)).isEqualTo(1);
        assertThat(hatchet.getCarriedByCharacterId()).isEqualTo("THING_PLAYER");
        assertThat(hatchet.getCurrentPlotId()).isNull();
        assertThat(index.getThingsInPlot("T_WRECK_BEACH")).noneMatch(t -> t.getId().equals("THING_HATCHET"));
        assertThat(res.getTurnContext().plotContext.visibleThings)
                .noneMatch(tc -> "rusty hatchet".equalsIgnoreCase(tc.getName()));
    }

    @Test
    void takeFailsWhenItemNotHere() {
        GameSession session = GameSession.newSession();
        session.setLocation(new com.demo.island.world.PlayerLocation("T_CAMP"));

        GameActionResult res = GameEngine.perform(session, GameAction.withItem(GameActionType.PICK_UP, GameItemType.HATCHET));

        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("haven't found any items here");
        assertThat(session.getInventory().getOrDefault(GameItemType.HATCHET, 0)).isZero();
    }
}
