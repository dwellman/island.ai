package com.demo.island.config;

import com.demo.island.ai.SpringAiDmAgent;
import com.demo.island.ai.SpringAiGhostAgent;
import com.demo.island.ai.SpringAiMonkeyAgent;
import com.demo.island.ai.SpringAiPlayerAgent;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.SimpleDmStubAgent;
import com.demo.island.engine.check.CheckService;
import com.demo.island.ghost.GhostAgent;
import com.demo.island.monkey.MonkeyAgent;
import com.demo.island.player.PlayerAgent;
import com.demo.island.tools.DmTools;
import com.demo.island.tools.GhostTools;
import com.demo.island.tools.MonkeyTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public DmTools dmTools() {
        return new DmTools();
    }

    @Bean
    public GhostTools ghostTools() {
        return new GhostTools();
    }

    @Bean
    public MonkeyTools monkeyTools() {
        return new MonkeyTools();
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    public SpringAiDmAgent dmAgent(ChatClient chatClient, CheckService checkService, DmTools dmTools) {
        return new SpringAiDmAgent(chatClient, checkService, dmTools);
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    public GhostAgent ghostAgent(ChatClient chatClient, GhostTools ghostTools) {
        return new SpringAiGhostAgent(chatClient, ghostTools);
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    public MonkeyAgent monkeyAgent(ChatClient chatClient, MonkeyTools monkeyTools) {
        return new SpringAiMonkeyAgent(chatClient, monkeyTools);
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    public PlayerAgent playerAgent(ChatClient chatClient) {
        return new SpringAiPlayerAgent(chatClient);
    }

    // Fallbacks when ChatClient is not available (no API key); use simple stubs to keep the app runnable.
    @Bean
    @ConditionalOnMissingBean(DmAgent.class)
    public DmAgent stubDmAgent() {
        return new SimpleDmStubAgent();
    }

    @Bean
    @ConditionalOnMissingBean(GhostAgent.class)
    public GhostAgent stubGhostAgent() {
        return input -> null;
    }

    @Bean
    @ConditionalOnMissingBean(MonkeyAgent.class)
    public MonkeyAgent stubMonkeyAgent() {
        return input -> null;
    }

    @Bean
    @ConditionalOnMissingBean(PlayerAgent.class)
    public PlayerAgent stubPlayerAgent() {
        return input -> "LOOK";
    }
}
