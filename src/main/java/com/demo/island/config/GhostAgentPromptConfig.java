package com.demo.island.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GhostAgentPromptConfig {

    private static final Logger LOG = LogManager.getLogger(GhostAgentPromptConfig.class);
    private static final String DEFAULT_PATH = "prompts/Ghost1.md";
    private static final String FALLBACK_PROMPT = "";

    @Bean(name = "ghostAgentSystemPrompt")
    public String ghostAgentSystemPrompt() {
        String overridePath = System.getProperty("ghost1.prompt.path");
        if (overridePath == null || overridePath.isBlank()) {
            overridePath = System.getenv("GHOST1_PROMPT_PATH");
        }
        String prompt = PromptLoader.loadOrFallback(DEFAULT_PATH, overridePath, FALLBACK_PROMPT, LOG, "Ghost1");
        if (prompt == null || prompt.isBlank()) {
            LOG.warn("Ghost1 prompt could not be loaded; disabling LLM ghost.");
        }
        return prompt;
    }
}
