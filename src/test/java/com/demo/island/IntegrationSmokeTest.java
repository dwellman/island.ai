package com.demo.island;

import com.demo.island.core.WorldState;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.PlayerCommand;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.TurnEngine;
import com.demo.island.engine.check.CheckService;
import com.demo.island.engine.dice.DiceService;
import com.demo.island.ghost.GhostAgent;
import com.demo.island.monkey.MonkeyAgent;
import com.demo.island.store.GameRepository;
import com.demo.island.store.InMemoryGameRepository;
import com.demo.island.world.WorldFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationSmokeTest.TestConfig.class)
class IntegrationSmokeTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        GameRepository gameRepository() {
            return new InMemoryGameRepository();
        }

        @Bean
        DiceService diceService() {
            return new DiceService();
        }

        @Bean
        CheckService checkService(DiceService diceService) {
            return new CheckService(diceService);
        }

        @Bean
        DmAgent dmAgent() {
            return new SimpleDmStubAgent();
        }

        @Bean
        GhostAgent ghostAgent() {
            return input -> null; // no-op ghost for smoke test
        }

        @Bean
        MonkeyAgent monkeyAgent() {
            return input -> null; // no-op monkey for smoke test
        }

        @Bean
        TurnEngine turnEngine(DiceService diceService, CheckService checkService,
                              GhostAgent ghostAgent, MonkeyAgent monkeyAgent) {
            return new TurnEngine(diceService, checkService, ghostAgent, monkeyAgent);
        }
    }

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DmAgent dmAgent;

    @Autowired
    private TurnEngine turnEngine;

    @Test
    void contextLoadsAndRunsSingleTurn() {
        WorldState worldState = WorldFactory.createDemoWorld("smoke-session");
        gameRepository.createNewSession(worldState);

        turnEngine.runTurn(worldState, new PlayerCommand("player-1", "LOOK"), dmAgent);

        assertThat(worldState.getPlayers()).containsKey("player-1");
        assertThat(worldState.getTiles()).containsKey("T_CAMP");
    }
}
