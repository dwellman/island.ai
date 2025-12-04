package com.demo.island.player;

import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.sim.SmartAiTestAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerAgentSpringConfig {

    @Bean
    @ConditionalOnBean(ChatClient.class)
    @ConditionalOnProperty(name = "unna.player.llm.enabled", havingValue = "true")
    public ExternalPlayerAgent llmExternalPlayerAgent(ChatClient chatClient,
                                                      String playerAgentSystemPrompt,
                                                      java.util.Map<com.demo.island.game.PlayerTool, ToolPrompt> playerToolPrompts) {
        return new LlmExternalPlayerAgent(chatClient, playerAgentSystemPrompt, playerToolPrompts);
    }

    @Bean
    @ConditionalOnMissingBean(ExternalPlayerAgent.class)
    public ExternalPlayerAgent heuristicExternalPlayerAgent() {
        return new SmartAiTestAgent();
    }
}
