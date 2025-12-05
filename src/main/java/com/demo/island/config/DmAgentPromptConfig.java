package com.demo.island.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DmAgentPromptConfig {

    private static final Logger LOG = LogManager.getLogger(DmAgentPromptConfig.class);
    private static final String DEFAULT_PATH = "prompts/dm1.md";
    private static final String FALLBACK_PROMPT = "";

    @Bean(name = "dmAgentSystemPrompt")
    public String dmAgentSystemPrompt() {
        String overridePath = System.getProperty("dm1.prompt.path");
        if (overridePath == null || overridePath.isBlank()) {
            overridePath = System.getenv("DM1_PROMPT_PATH");
        }
        return PromptLoader.loadOrFallback(DEFAULT_PATH, overridePath, FALLBACK_PROMPT, LOG, "DM1");
    }
}
