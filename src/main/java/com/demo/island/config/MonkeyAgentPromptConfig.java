package com.demo.island.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonkeyAgentPromptConfig {

    private static final Logger LOG = LogManager.getLogger(MonkeyAgentPromptConfig.class);
    private static final String DEFAULT_PATH = "prompts/Monkey1.md";
    private static final String FALLBACK_PROMPT = "";

    @Bean(name = "monkeyAgentSystemPrompt")
    public String monkeyAgentSystemPrompt() {
        String overridePath = System.getProperty("monkey1.prompt.path");
        if (overridePath == null || overridePath.isBlank()) {
            overridePath = System.getenv("MONKEY1_PROMPT_PATH");
        }
        return PromptLoader.loadOrFallback(DEFAULT_PATH, overridePath, FALLBACK_PROMPT, LOG, "Monkey1");
    }
}
