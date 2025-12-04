package com.demo.island.config;

import com.demo.island.engine.TurnEngine;
import com.demo.island.engine.check.CheckService;
import com.demo.island.engine.dice.DiceService;
import com.demo.island.ghost.GhostAgent;
import com.demo.island.monkey.MonkeyAgent;
import com.demo.island.store.GameRepository;
import com.demo.island.store.InMemoryGameRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

    @Bean
    public DiceService diceService() {
        return new DiceService();
    }

    @Bean
    public CheckService checkService(DiceService diceService) {
        return new CheckService(diceService);
    }

    @Bean
    public TurnEngine turnEngine(DiceService diceService, CheckService checkService,
                                 GhostAgent ghostAgent, MonkeyAgent monkeyAgent) {
        return new TurnEngine(diceService, checkService, ghostAgent, monkeyAgent);
    }

    @Bean
    public GameRepository gameRepository() {
        return new InMemoryGameRepository();
    }
}
